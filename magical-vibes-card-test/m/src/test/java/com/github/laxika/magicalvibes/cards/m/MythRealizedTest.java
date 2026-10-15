package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythRealized.class, SpidersilkNet.class, ColossodonYearling.class, DressDown.class})
class MythRealizedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a lore counter on Myth Realized")
    void noncreatureSpellAddsLoreCounter() {
        Permanent myth = addMythReady();
        harness.setHand(player1, List.of(new SpidersilkNet()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put a lore counter on Myth Realized")
    void creatureSpellDoesNotAddLoreCounter() {
        Permanent myth = addMythReady();
        harness.setHand(player1, List.of(new ColossodonYearling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("The activated ability puts a lore counter on Myth Realized")
    void activatedAbilityAddsLoreCounter() {
        Permanent myth = addMythReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Myth Realized becomes a dynamic Monk Avatar creature")
    void activatesAnimation() {
        Permanent myth = addMythReady();
        myth.setCounterCount(CounterType.LORE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, myth)).isTrue();
        assertThat(gqs.isEnchantment(gd, myth)).isTrue();
        assertThat(gqs.getEffectivePower(gd, myth)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myth)).isEqualTo(3);
        assertThat(myth.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.MONK, CardSubtype.AVATAR);

        myth.setCounterCount(CounterType.LORE, 5);
        assertThat(gqs.getEffectivePower(gd, myth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, myth)).isEqualTo(5);
    }

    @Test
    @DisplayName("Myth Realized stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent myth = addMythReady();
        myth.setCounterCount(CounterType.LORE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, myth)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, myth)).isFalse();
        assertThat(gqs.isEnchantment(gd, myth)).isTrue();
        assertThat(myth.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not add a lore counter")
    void opponentSpellDoesNotAddLoreCounter() {
        Permanent myth = addMythReady();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SpidersilkNet()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("Animating with no lore counters puts Myth Realized into the graveyard")
    void animationWithoutCountersDies() {
        addMythReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Myth Realized");
        harness.assertInGraveyard(player1, "Myth Realized");
    }

    @Test
    @DisplayName("Adding a lore counter through the activated ability increases animated power and toughness")
    void activatedCounterUpdatesAnimatedPowerToughness() {
        Permanent myth = addMythReady();
        myth.setCounterCount(CounterType.LORE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, myth)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myth)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a noncreature spell while animated increases lore-based power and toughness")
    void castTriggerUpdatesAnimatedPowerToughness() {
        Permanent myth = addMythReady();
        myth.setCounterCount(CounterType.LORE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SpidersilkNet()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(myth.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, myth)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myth)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dress Down removes the granted lore-based ability from an already animated Myth Realized")
    void losingGrantedPowerToughnessAbilityDies() {
        Permanent myth = addMythReady();
        myth.setCounterCount(CounterType.LORE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, myth)).isEqualTo(3);

        harness.setHand(player2, List.of(new DressDown()));
        harness.setLibrary(player2, List.of(new ColossodonYearling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Myth Realized");
        harness.assertInGraveyard(player1, "Myth Realized");
    }

    private Permanent addMythReady() {
        Permanent myth = harness.addToBattlefieldAndReturn(player1, new MythRealized());
        myth.setSummoningSick(false);
        return myth;
    }
}
