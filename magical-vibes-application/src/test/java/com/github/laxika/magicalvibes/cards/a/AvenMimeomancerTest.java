package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvenMimeomancerTest extends BaseCardTest {

    // ===== Upkeep feather counter: base P/T set to 3/1 + flying =====

    @Test
    @DisplayName("Feather counter sets the creature's base P/T to 3/1 and grants flying")
    void featherCounterSetsBaseStatsAndGrantsFlying() {
        Permanent bears = castAvenWithBears(); // Grizzly Bears is a 2/2

        placeFeatherCounter(bears);

        assertThat(bears.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature untouched")
    void decliningLeavesCreatureUnchanged() {
        Permanent bears = castAvenWithBears();

        triggerUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.FEATHER)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    // ===== The effect persists once the source leaves (official ruling) =====

    @Test
    @DisplayName("Creature keeps base P/T 3/1 and flying after Aven Mimeomancer leaves the battlefield")
    void effectPersistsAfterAvenLeaves() {
        Permanent bears = castAvenWithBears();
        placeFeatherCounter(bears);

        // Aven Mimeomancer leaves — the feather-counter rule is source-independent, so it stays.
        Permanent aven = findAven();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(aven.getId()));
        gd.expireFloatingEffectsForDepartedSource(aven.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    // ===== Helpers =====

    /** Casts Aven Mimeomancer (resolving its enter-the-battlefield rule) and returns a 2/2 target. */
    private Permanent castAvenWithBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AvenMimeomancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Aven spell → enters
        harness.passBothPriorities(); // resolve ETB rule-establishing trigger
        return bears;
    }

    private void placeFeatherCounter(Permanent target) {
        triggerUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
    }

    private Permanent findAven() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Mimeomancer"))
                .findFirst().orElseThrow();
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, firing the trigger
    }
}
