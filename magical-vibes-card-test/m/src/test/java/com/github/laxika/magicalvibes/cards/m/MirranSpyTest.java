package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDigester;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirranSpy.class, GrizzlyBears.class, Spellbook.class, PhyrexianDigester.class})
class MirranSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact cast chooses a target before the optional resolution decision")
    void artifactCastChoosesTargetBeforeMayPrompt() {
        Permanent spy = harness.addToBattlefieldAndReturn(player1, new MirranSpy());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, spy.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(spy.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting at resolution untaps the chosen creature before the artifact resolves")
    void acceptUntapsTargetCreature() {
        harness.addToBattlefield(player1, new MirranSpy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Can target and untap an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new MirranSpy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining at resolution leaves the targeted creature tapped")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MirranSpy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Mirran Spy")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MirranSpy());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger Mirran Spy")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MirranSpy());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Noncreature artifacts are excluded from the trigger's legal targets")
    void cannotTargetNoncreatureArtifact() {
        Permanent spy = harness.addToBattlefieldAndReturn(player1, new MirranSpy());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        book.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(spy.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature cast triggers before that creature enters")
    void artifactCreatureCastTriggers() {
        Permanent spy = harness.addToBattlefieldAndReturn(player1, new MirranSpy());
        spy.tap();

        harness.castFromHand(player1, new PhyrexianDigester(), "{3}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(spy.getId());
        harness.handlePermanentChosen(player1, spy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spy.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Phyrexian Digester");
        assertThat(gd.stack).hasSize(1);
    }
    @Test
    @DisplayName("Mirran Spy can untap itself")
    void canUntapItself() {
        Permanent spy = harness.addToBattlefieldAndReturn(player1, new MirranSpy());
        spy.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, spy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spy.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already untapped creature is a legal target")
    void canTargetUntappedCreature() {
        harness.addToBattlefield(player1, new MirranSpy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }
}
