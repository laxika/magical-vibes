package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.a.AshiokNightmareWeaver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TymaretTheMurderKing.class, BronzeSable.class, AshiokNightmareWeaver.class})
class TymaretTheMurderKingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and deals 2 damage to a target player")
    void sacrificesAnotherCreatureAndDamagesTargetPlayer() {
        Permanent tymaret = harness.addToBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tymaret);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card == otherCreature.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature with the damage ability")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new TymaretTheMurderKing());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void cannotReturnWithoutACreatureToSacrifice() {
        harness.setGraveyard(player1, List.of(new TymaretTheMurderKing()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Tymaret, the Murder King");
        harness.assertNotInHand(player1, "Tymaret, the Murder King");
    }

    @Test
    void cannotSacrificeItselfForDamage() {
        harness.addToBattlefield(player1, new TymaretTheMurderKing());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Tymaret, the Murder King");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotUseOpponentsCreatureForGraveyardReturnCost() {
        harness.setGraveyard(player1, List.of(new TymaretTheMurderKing()));
        harness.addToBattlefield(player2, new BronzeSable());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bronze Sable");
        harness.assertInGraveyard(player1, "Tymaret, the Murder King");
    }

    @Test
    void damagesPlaneswalkerAndSacrificesCreatureBeforeResolution() {
        harness.addToBattlefield(player1, new TymaretTheMurderKing());
        harness.addToBattlefield(player1, new BronzeSable());
        Permanent ashiok = harness.addToBattlefieldAndReturn(player2, new AshiokNightmareWeaver());
        ashiok.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, ashiok.getId());

        harness.assertNotOnBattlefield(player1, "Bronze Sable");
        harness.assertInGraveyard(player1, "Bronze Sable");
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to its owner's hand")
    void returnsFromGraveyardToHand() {
        TymaretTheMurderKing tymaret = new TymaretTheMurderKing();
        BronzeSable unrelatedCard = new BronzeSable();
        harness.setGraveyard(player1, List.of(tymaret, unrelatedCard));
        harness.addToBattlefield(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bronze Sable");
        harness.assertInGraveyard(player1, "Bronze Sable");
        assertThat(gd.playerHands.get(player1.getId())).contains(tymaret);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tymaret);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unrelatedCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(unrelatedCard);
    }
}
