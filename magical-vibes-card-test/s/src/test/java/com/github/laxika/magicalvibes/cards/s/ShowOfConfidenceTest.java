package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowOfConfidence.class, CampusGuide.class, StudyBreak.class, ExpandedAnatomy.class, StrixhavenStadium.class})
class ShowOfConfidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target and grants vigilance until end of turn")
    void putsCounterAndGrantsVigilance() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Copies once for each prior instant or sorcery cast by its controller")
    void copiesForPriorInstantSorceriesOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        gd.recordSpellCast(player1.getId(), new StudyBreak());
        gd.recordSpellCast(player1.getId(), new CampusGuide());
        gd.recordSpellCast(player2.getId(), new StudyBreak());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(entry -> entry.getCard().getName().equals("Show of Confidence"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void copiesCountInstantsCastInResponseToTheTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        harness.setHand(player1, List.of(new ShowOfConfidence(), new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void instantAndSorceryCopiesResolveWithUnchangedTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        gd.recordSpellCast(player1.getId(), new StudyBreak());
        gd.recordSpellCast(player1.getId(), new ExpandedAnatomy());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(gd.getSpellsCastThisTurn(player1.getId())).hasSize(3);
    }

    @Test
    void copyCanTargetAnOpponentsCreatureIndependently() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        gd.recordSpellCast(player1.getId(), new StudyBreak());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(copyTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(originalTarget.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(copyTarget.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void copyCanChooseNewTargetWhenOriginalTargetHasLeftBattlefield() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        gd.recordSpellCast(player1.getId(), new StudyBreak());
        harness.setHand(player1, List.of(new ShowOfConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        gd.playerBattlefields.get(player1.getId()).remove(originalTarget);
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(copyTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(copyTarget.hasKeyword(Keyword.VIGILANCE)).isTrue();
        harness.assertInGraveyard(player1, "Show of Confidence");
    }
}
