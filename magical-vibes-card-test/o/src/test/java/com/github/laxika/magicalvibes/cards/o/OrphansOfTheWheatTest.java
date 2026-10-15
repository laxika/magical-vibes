package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrphansOfTheWheat.class})
class OrphansOfTheWheatTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts to tap untapped creatures and boosts Orphans of the Wheat")
    void attackTriggerTapsCreaturesAndBoostsSource() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent firstCreature = addReadyCreature(new OrphansOfTheWheat());
        Permanent secondCreature = addReadyCreature(new OrphansOfTheWheat());

        declareAttack(orphans);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(3);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the source unchanged")
    void choosingNoCreaturesDoesNotBoostSource() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent creature = addReadyCreature(new OrphansOfTheWheat());

        declareAttack(orphans);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent creature = addReadyCreature(new OrphansOfTheWheat());

        declareAttack(orphans);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning-sick creatures can be tapped, but tapped and opposing creatures cannot")
    void onlyUntappedControlledCreaturesAreOffered() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent sickCreature = harness.addToBattlefieldAndReturn(player1, new OrphansOfTheWheat());
        sickCreature.setSummoningSick(true);
        Permanent unchosenCreature = addReadyCreature(new OrphansOfTheWheat());
        Permanent tappedCreature = addReadyCreature(new OrphansOfTheWheat());
        tappedCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OrphansOfTheWheat());

        declareAttack(orphans);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(sickCreature.getId(), unchosenCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(sickCreature.getId()));

        assertThat(sickCreature.isTapped()).isTrue();
        assertThat(unchosenCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(2);
    }

    @Test
    @DisplayName("An attack with no untapped creatures resolves without a choice or boost")
    void noUntappedCreaturesDoesNotPrompt() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());

        declareAttack(orphans);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(1);
    }

    @Test
    @DisplayName("Orphans can tap itself if it is untapped before its attack trigger resolves")
    void untappedSourceCanTapItself() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttack(orphans));
        orphans.untap();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(orphans.getId()));

        assertThat(orphans.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player1, card);
    }

    private void declareAttack(Permanent orphans) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(orphans)));
    }
}
