package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroicReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Soldiers and gives own creatures +1/+1 and haste")
    void createsSoldiersAndBuffsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHeroicReinforcements();

        List<Permanent> soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getEffectivePower()).isEqualTo(2);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(2);
            assertThat(soldier.hasKeyword(Keyword.HASTE)).isTrue();
        });

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The boost and haste last until end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHeroicReinforcements();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER)))
                .allSatisfy(soldier -> {
                    assertThat(soldier.getEffectivePower()).isEqualTo(1);
                    assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
                    assertThat(soldier.hasKeyword(Keyword.HASTE)).isFalse();
                });
    }

    private void castHeroicReinforcements() {
        harness.setHand(player1, List.of(new HeroicReinforcements()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
