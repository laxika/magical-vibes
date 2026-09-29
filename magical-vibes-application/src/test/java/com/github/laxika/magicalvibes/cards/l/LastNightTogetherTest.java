package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastNightTogether.class, GrizzlyBears.class, HillGiant.class})
class LastNightTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, strengthens, and grants keywords to both chosen creatures")
    void buffsBothChosenCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        bears.tap();
        giant.tap();

        castLastNightTogether(bears, giant);

        for (Permanent creature : List.of(bears, giant)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("The added combat allows only the chosen creatures to attack")
    void restrictsTheAdditionalCombatToChosenCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castLastNightTogether(bears, giant);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        gs.advanceStep(gd);

        PendingInteraction.AttackerDeclaration prompt = gd.interaction.activeInteraction(
                PendingInteraction.AttackerDeclaration.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.attackerIndices()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(giant));
        assertThat(prompt.attackerIndices()).doesNotContain(
                gd.playerBattlefields.get(player1.getId()).indexOf(unchosen));
    }

    private void castLastNightTogether(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new LastNightTogether()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }
}
