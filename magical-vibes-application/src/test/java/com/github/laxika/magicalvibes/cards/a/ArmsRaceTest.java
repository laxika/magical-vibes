package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmsRaceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only artifact cards in hand")
    void abilityOffersOnlyArtifacts() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Chosen artifact enters with haste and is scheduled for end-step sacrifice")
    void chosenArtifactEntersWithHasteAndEndStepSacrifice() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(ornithopter.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Declining the may leaves the artifact in hand")
    void decliningLeavesArtifactInHand() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    private Permanent addReadyArmsRace() {
        Permanent armsRace = new Permanent(new ArmsRace());
        armsRace.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(armsRace);
        return armsRace;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
