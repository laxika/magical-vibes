package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SpreadingInsurrection.class, GrizzlyBears.class})
class SpreadingInsurrectionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of, untaps, and grants haste to an opponent's creature")
    void resolvesControlUntapAndHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castSpreadingInsurrection(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Temporary control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSpreadingInsurrection(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Only an opponent's creature can be targeted")
    void rejectsOwnCreatureAsTarget() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before it")
    void stormCopiesForEachPriorSpell() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm copies may each steal a different creature without being cast")
    void stormCopiesMayChooseDifferentCreatures() {
        Permanent originalTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstCopyTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCopyTarget = addCreatureReady(player2, new GrizzlyBears());
        originalTarget.tap();
        firstCopyTarget.tap();
        secondCopyTarget.tap();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();
        harness.castSorcery(player1, 0, originalTarget.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopyTarget.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondCopyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(originalTarget, firstCopyTarget, secondCopyTarget);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(originalTarget, firstCopyTarget, secondCopyTarget);
        for (Permanent target : List.of(originalTarget, firstCopyTarget, secondCopyTarget)) {
            assertThat(target.isTapped()).isFalse();
            assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
            assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        }
    }

    @Test
    @DisplayName("The original cannot untap a creature already stolen by its storm copy")
    void originalTargetBecomesIllegalAfterCopyStealsIt() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
        target.tap();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Storm ignores spells cast after Spreading Insurrection")
    void stormCountIsFixedAtCastTime() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("A departed target receives none of the spell's effects")
    void departedTargetIsNotStolenUntappedOrGrantedHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    private void castSpreadingInsurrection(Permanent target) {
        harness.setHand(player1, List.of(new SpreadingInsurrection()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
