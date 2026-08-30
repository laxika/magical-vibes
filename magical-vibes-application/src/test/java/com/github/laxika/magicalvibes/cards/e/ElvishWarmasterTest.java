package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishWarmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elf Warrior when another Elf enters, only once each turn")
    void createsTokenOncePerTurnForElfEntry() {
        harness.setHand(player1, List.of(new ElvishWarmaster()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Activated ability boosts all Elves you control and grants them deathtouch")
    void activatedAbilityBoostsElvesAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent warmaster = findPermanent(player1, "Elvish Warmaster");
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Activated ability's boost and deathtouch wear off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent warmaster = findPermanent(player1, "Elvish Warmaster");
        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warmaster)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isFalse();
    }
}
