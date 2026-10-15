package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BassaraTowerArcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PolymorphousRush.class, GrizzlyBears.class, ProdigalSorcerer.class, BassaraTowerArcher.class})
class PolymorphousRushTest extends BaseCardTest {

    @Test
    void chosenCreatureSuppliesAbilitiesToEachTargetYouControl() {
        Permanent chosenCreature = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        castWithMana();

        harness.castInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenCreature.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void striveChargesForEachAdditionalTarget() {
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PolymorphousRush()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiesExpireAtEndOfTurn() {
        Permanent chosenCreature = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castWithMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenCreature.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetsMustBeCreaturesYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PolymorphousRush()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void canBeCastWithNoTargetsForItsBaseCost() {
        harness.setHand(player1, List.of(new PolymorphousRush()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Polymorphous Rush");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseAnOpponentsHexproofCreatureBecauseTheChoiceDoesNotTarget() {
        Permanent chosen = addCreatureReady(player2, new BassaraTowerArcher());
        Permanent target = addCreatureReady(player1, new ProdigalSorcerer());
        castWithMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(target.getCard().getName()).isEqualTo("Bassara Tower Archer");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aTargetCanBeTheChosenCreature() {
        Permanent chosen = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        castWithMana();

        harness.castInstant(player1, 0, List.of(chosen.getId(), other.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void sourceCountersAreNotCopiedAndTargetCountersRemain() {
        Permanent chosen = addCreatureReady(player2, new ProdigalSorcerer());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castWithMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void aTargetNoLongerControlledByCasterIsUnaffectedWhileOtherTargetCopies() {
        Permanent chosen = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent changedController = addCreatureReady(player1, new GrizzlyBears());
        Permanent legalTarget = addCreatureReady(player1, new GrizzlyBears());
        castWithMana();

        harness.castInstant(player1, 0, List.of(changedController.getId(), legalTarget.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(changedController);
        gd.playerBattlefields.get(player2.getId()).add(changedController);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotChooseACreatureWhenItsOnlyTargetIsIllegal() {
        addCreatureReady(player2, new ProdigalSorcerer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castWithMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Polymorphous Rush");
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithMana() {
        harness.setHand(player1, List.of(new PolymorphousRush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);
    }
}
