package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalkingYeti.class, KrovikanScoundrel.class})
class StalkingYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and fights a target creature an opponent controls")
    void entersAndFightsOpponentCreature() {
        Permanent opponentScoundrel = addCreatureReady(player2, new KrovikanScoundrel());

        castYeti();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentScoundrel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Krovikan Scoundrel");
        assertThat(opponentScoundrel.getMarkedDamage()).isEqualTo(3);
        Permanent yeti = findPermanent(player1, "Stalking Yeti");
        assertThat(yeti.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownScoundrel = addCreatureReady(player1, new KrovikanScoundrel());
        Permanent opponentScoundrel = addCreatureReady(player2, new KrovikanScoundrel());

        castYeti();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownScoundrel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentScoundrel.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ETB ability does nothing if Stalking Yeti leaves before it resolves")
    void etbDoesNothingIfYetiLeavesBeforeResolution() {
        Permanent opponentScoundrel = addCreatureReady(player2, new KrovikanScoundrel());

        castYeti();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentScoundrel.getId());
        Permanent yeti = findPermanent(player1, "Stalking Yeti");
        gd.playerBattlefields.get(player1.getId()).remove(yeti);
        harness.passBothPriorities();

        assertThat(opponentScoundrel.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Snow mana activates the self-bounce ability")
    void snowManaReturnsItselfToHand() {
        Permanent yeti = addCreatureReady(player1, new StalkingYeti());
        addAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, yeti), null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stalking Yeti");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("The self-bounce ability can activate only as a sorcery")
    void selfBounceRequiresSorcerySpeed() {
        Permanent yeti = addCreatureReady(player1, new StalkingYeti());
        addAbilityMana(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, yeti), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The self-bounce ability cannot activate outside a main phase")
    void selfBounceRequiresMainPhase() {
        Permanent yeti = addCreatureReady(player1, new StalkingYeti());
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, yeti), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        Permanent yeti = addCreatureReady(player1, new StalkingYeti());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, yeti), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void castYeti() {
        harness.setHand(player1, List.of(new StalkingYeti()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
