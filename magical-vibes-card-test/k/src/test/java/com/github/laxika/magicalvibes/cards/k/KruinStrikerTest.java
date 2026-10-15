package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.ScaldingDevil;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KruinStriker.class, ScaldingDevil.class, ThatcherRevolt.class})
class KruinStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering gives it +1/+0 and trample")
    void pumpsAndGrantsTrampleOnAllyEnter() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());

        harness.setHand(player1, List.of(new ScaldingDevil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passUntil(TurnStep.END_STEP);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());

        harness.setHand(player1, List.of(new ScaldingDevil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passUntil(TurnStep.END_STEP);

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void noTriggerForOpponentCreature() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ScaldingDevil()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("One creature entry creates one ability that grants both effects together")
    void boostAndTrampleResolveAsOneAbility() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());
        harness.setHand(player1, List.of(new ScaldingDevil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kruin Striker does not trigger for its own entry")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new KruinStriker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent striker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Each creature token entering adds a separate cumulative boost")
    void creatureTokensEachTriggerAndBoostsAccumulate() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0);
        harness.passUntil(TurnStep.END_STEP);

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }
}
