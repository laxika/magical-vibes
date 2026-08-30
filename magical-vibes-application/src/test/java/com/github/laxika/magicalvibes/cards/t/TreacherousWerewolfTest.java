package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreacherousWerewolf.class, FlameSlash.class, Spellbook.class})
class TreacherousWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with seven cards in its controller's graveyard")
    void getsThresholdBoost() {
        fillGraveyard(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get +2/+2 below threshold")
    void noThresholdBoostBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold death ability makes its controller lose 4 life")
    void thresholdDeathAbilityLosesLife() {
        fillGraveyard(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        killWithFlameSlash(werewolf);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Threshold death ability is absent below seven graveyard cards")
    void thresholdDeathAbilityIsAbsentBelowThreshold() {
        fillGraveyard(player1, 6);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        killWithFlameSlash(werewolf);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void killWithFlameSlash(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameSlash()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
