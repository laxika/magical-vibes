package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeilOfAssimilation.class, AccordersShield.class, GrizzlyBears.class, CopperLonglegs.class})
class VeilOfAssimilationTest extends BaseCardTest {

    @Test
    void itsEntryBoostsAndGrantsVigilanceToTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVeil();

        chooseOwnEntryTarget(bears);

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void anotherArtifactEntryBoostsAndGrantsVigilanceToTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new VeilOfAssimilation());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        chooseOtherArtifactTarget(bears);

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void targetMustBeCreatureYouControl() {
        harness.addToBattlefield(player1, new VeilOfAssimilation());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        chooseOtherArtifactTarget(ownBears);
        assertThat(ownBears.getEffectivePower()).isEqualTo(3);
        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void boostAndVigilanceWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVeil();
        chooseOwnEntryTarget(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void repeatedEntriesStackTheirBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        castVeil();
        chooseOwnEntryTarget(creature);

        harness.setHand(player1, List.of(new AccordersShield(), new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        chooseOtherArtifactTarget(creature);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        chooseOtherArtifactTarget(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void opponentsArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new VeilOfAssimilation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new VeilOfAssimilation()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void nonartifactCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new VeilOfAssimilation());
        harness.setHand(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(harness.getPermanentId(player1, "Copper Longlegs")))
                .findFirst().orElseThrow();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void entryWithoutLegalCreatureTargetDoesNotLeavePendingChoice() {
        castVeil();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Veil of Assimilation");
    }

    private void castVeil() {
        harness.setHand(player1, List.of(new VeilOfAssimilation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseOwnEntryTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenTargetTrigger.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void chooseOtherArtifactTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
