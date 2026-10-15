package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroicReinforcements.class, GreenwoodSentinel.class})
class HeroicReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Soldiers and gives own creatures +1/+1 and haste")
    void createsSoldiersAndBuffsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        castHeroicReinforcements();

        List<Permanent> soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.isTapped()).isFalse();
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
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

    @Test
    @DisplayName("Creatures entering after resolution do not get either bonus")
    void laterCreaturesAreNotAffected() {
        castHeroicReinforcements();

        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();

        Permanent laterCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GreenwoodSentinel)
                .findFirst().orElseThrow();
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(laterCreature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2)
                .allSatisfy(soldier -> {
                    assertThat(soldier.getEffectivePower()).isEqualTo(2);
                    assertThat(soldier.getEffectiveToughness()).isEqualTo(2);
                    assertThat(soldier.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("Repeated casts stack the boost on existing creatures and create new Soldiers")
    void repeatedCastsBoostExistingCreaturesAgain() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        castHeroicReinforcements();
        List<Permanent> firstSoldiers = List.copyOf(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList());

        castHeroicReinforcements();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(firstSoldiers).hasSize(2).allSatisfy(soldier -> {
            assertThat(soldier.getEffectivePower()).isEqualTo(3);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(3);
            assertThat(soldier.hasKeyword(Keyword.HASTE)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> !firstSoldiers.contains(permanent)))
                .hasSize(2)
                .allSatisfy(soldier -> {
                    assertThat(soldier.getEffectivePower()).isEqualTo(2);
                    assertThat(soldier.getEffectiveToughness()).isEqualTo(2);
                    assertThat(soldier.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    private void castHeroicReinforcements() {
        harness.castFromHand(player1, new HeroicReinforcements(), "{2}{R}{W}");
        harness.passBothPriorities();
    }
}
