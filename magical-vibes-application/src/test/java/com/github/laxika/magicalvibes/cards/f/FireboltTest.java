package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Firebolt.class, EmberBeast.class, Mountain.class})
class FireboltTest extends BaseCardTest {

    @Test
    @DisplayName("Firebolt deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Firebolt deals 2 damage to target creature")
    void deals2DamageToCreature() {
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EmberBeast());

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback from graveyard deals 2 damage and exiles Firebolt")
    void flashbackDealsDamageAndExiles() {
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Firebolt");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Firebolt"));
    }

    @Test
    @DisplayName("Cannot cast Firebolt with flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new Firebolt()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires red mana in addition to four generic mana")
    void flashbackRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Firebolt deals 2 damage to a target planeswalker")
    void deals2DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @CardUsed(InvasionOfInnistrad.class)
    @DisplayName("Firebolt deals 2 damage to a target battle")
    void deals2DamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, battle.getId());

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Firebolt cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
