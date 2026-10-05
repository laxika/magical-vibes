package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MysticReflection;
import com.github.laxika.magicalvibes.cards.s.StaffOfTheStoryteller;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueOfVermin.class, StaffOfTheStoryteller.class, PlatinumEmperion.class, MysticReflection.class})
class PlagueOfVerminTest extends BaseCardTest {

    private void cast(Player caster) {
        PlagueOfVermin plague = new PlagueOfVermin();
        plague.setSetCode("SHM");
        plague.setCollectorNumber("73");
        harness.setHand(caster, List.of(plague));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(caster, 0, 0);
    }

    private long ratCount(Player player) {
        return countPermanents(player, "Rat");
    }

    @Test
    @DisplayName("Each player creates a Rat for each life paid, over multiple rounds; life is paid immediately")
    void paysLifeOverRoundsCreatesRats() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(player1);

        // Round 1: controller pays 3, opponent pays 2 (either payment resets the pass counter).
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 2);
        // Round 2: both decline — two consecutive passes ends the process.
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        assertThat(ratCount(player1)).isEqualTo(3);
        assertThat(ratCount(player2)).isEqualTo(2);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(rat.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
    }

    @Test
    @DisplayName("A single decline does not end the process if a later player pays")
    void continuesAfterAPlayerDeclines() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(player1);

        harness.handleXValueChosen(player1, 0);
        harness.assertLife(player1, 20);
        harness.handleXValueChosen(player2, 2);
        harness.assertLife(player2, 18);
        harness.handleXValueChosen(player1, 3);
        harness.assertLife(player1, 17);
        harness.handleXValueChosen(player2, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleXValueChosen(player2, 2);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ratCount(player1)).isEqualTo(3);
        assertThat(ratCount(player2)).isEqualTo(4);
    }

    @Test
    @DisplayName("The spell's controller gets the first life-payment choice")
    void startsWithTheController() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(player2);

        harness.handleXValueChosen(player2, 1);
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 18);
        assertThat(ratCount(player2)).isEqualTo(1);
        assertThat(ratCount(player1)).isEqualTo(2);
    }

    @Test
    @CardUsed({PlagueOfVermin.class, StaffOfTheStoryteller.class})
    @DisplayName("Creating Rats triggers an ability that watches for creature tokens")
    void triggersCreatureTokenCreationAbilities() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(player1);

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 1);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);
        resolveAllTriggers();

        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    @DisplayName("If no one pays life, no tokens are created and no life is lost")
    void noOnePaysCreatesNoTokens() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(player1);

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(ratCount(player1)).isZero();
        assertThat(ratCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player at 0 life can no longer pay and is skipped, ending the process")
    void playerAtZeroLifeIsSkipped() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 1);

        cast(player1);

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 1); // spends its last life
        harness.handleXValueChosen(player1, 0); // controller declines; opponent at 0 is auto-skipped

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 0);
        assertThat(ratCount(player1)).isEqualTo(5);
        assertThat(ratCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life payments accumulate across rounds and Rats enter only after all payments finish")
    void accumulatesPaymentsBeforeCreatingTokens() {
        cast(player1);

        harness.handleXValueChosen(player1, 2);
        harness.assertLife(player1, 18);
        harness.handleXValueChosen(player2, 1);
        harness.handleXValueChosen(player1, 3);
        harness.assertLife(player1, 15);
        harness.handleXValueChosen(player2, 2);
        harness.assertLife(player2, 17);
        assertThat(ratCount(player1)).isZero();
        assertThat(ratCount(player2)).isZero();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(ratCount(player1)).isEqualTo(5);
        assertThat(ratCount(player2)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Plague of Vermin");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A player cannot pay more life than they currently have")
    void rejectsPaymentAboveCurrentLife() {
        harness.setLife(player1, 5);
        cast(player1);

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 6))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertLife(player1, 5);
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 1);
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 4))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertLife(player1, 3);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(ratCount(player1)).isEqualTo(2);
        assertThat(ratCount(player2)).isEqualTo(1);
    }

    @Test
    @CardUsed({PlagueOfVermin.class, PlatinumEmperion.class})
    @DisplayName("A player whose life total cannot change may only pay zero life")
    void cannotPayLifeWithPlatinumEmperion() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        cast(player1);

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        if (choice.playerId().equals(player1.getId())) {
            assertThat(choice.maxValue()).isZero();
            assertThatThrownBy(() -> harness.handleXValueChosen(player1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            harness.handleXValueChosen(player1, 0);
        }
        harness.handleXValueChosen(player2, 2);
        choice = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        if (choice.playerId().equals(player1.getId())) {
            assertThat(choice.maxValue()).isZero();
            harness.handleXValueChosen(player1, 0);
        }
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(ratCount(player1)).isZero();
        assertThat(ratCount(player2)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PlagueOfVermin.class, PlatinumEmperion.class, MysticReflection.class})
    @DisplayName("All Rats enter together and receive the same next-entry replacement")
    void allRatsEnterSimultaneouslyWithMysticReflection() {
        Permanent emperion = harness.addToBattlefieldAndReturn(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, emperion.getId());

        cast(player1);
        harness.handleXValueChosen(player1, 3);
        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        if (choice.playerId().equals(player2.getId())) {
            harness.handleXValueChosen(player2, 0);
        }
        harness.handleXValueChosen(player1, 0);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleXValueChosen(player2, 0);
        }

        assertThat(countPermanents(player1, "Platinum Emperion")).isEqualTo(3);
        assertThat(ratCount(player1)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
