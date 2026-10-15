package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WantedGriffin.class, Shock.class, GrizzlyBears.class})
class WantedGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("When Wanted Griffin dies, it creates a Mercenary token")
    void deathTriggerCreatesMercenaryToken() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());

        destroyWithShock(griffin);

        harness.assertInGraveyard(player1, "Wanted Griffin");
        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The Mercenary token can only be activated at sorcery speed")
    void mercenaryRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The death trigger creates an untapped red 1/1 Mercenary creature token")
    void mercenaryHasCorrectCreatedCharacteristics() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        assertThat(mercenary.getCard().isToken()).isTrue();
        assertThat(mercenary.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(mercenary.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(mercenary.getCard().getSubtypes()).containsExactly(CardSubtype.MERCENARY);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Mercenary")).isEmpty();
    }

    @Test
    @DisplayName("A newly created Mercenary cannot pay its tap cost")
    void mercenaryCannotActivateWithSummoningSickness() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Mercenary can boost itself, and the boost expires at end of turn")
    void mercenaryCanBoostItselfUntilEndOfTurn() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        destroyWithShock(griffin);
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
    }

    private void destroyWithShock(Permanent griffin) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, griffin.getId());
        harness.passBothPriorities();
    }
}
