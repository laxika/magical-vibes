package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallaxDementia.class, SealOfCleansing.class, KorHaven.class, SpinelessThug.class})
class ParallaxDementiaTest extends BaseCardTest {

    @Test
    @DisplayName("Parallax Dementia enters with a fade counter and boosts the enchanted creature")
    void entersWithFadeCounterAndBoostsCreature() {
        Permanent thug = addCreatureReady(player2, new SpinelessThug());

        harness.setHand(player1, List.of(new ParallaxDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, thug.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Parallax Dementia");
        assertThat(aura.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thug)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thug)).isEqualTo(4);
    }

    @Test
    @DisplayName("Parallax Dementia removes its fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent thug = addCreatureReady(player1, new SpinelessThug());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(thug.getId());
        aura.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Parallax Dementia");
    }

    @Test
    @DisplayName("Parallax Dementia sacrifices itself at upkeep with no fade counters")
    void sacrificesWithNoFadeCounters() {
        Permanent thug = addCreatureReady(player1, new SpinelessThug());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(thug.getId());
        aura.setCounterCount(CounterType.FADE, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Parallax Dementia");
        harness.assertInGraveyard(player1, "Spineless Thug");
    }

    @Test
    @DisplayName("When Parallax Dementia leaves, it destroys the enchanted creature without regeneration")
    void destroysEnchantedCreatureWhenAuraLeaves() {
        Permanent thug = addCreatureReady(player2, new SpinelessThug());
        thug.setRegenerationShield(1);
        harness.addToBattlefieldAndReturn(player1, new SealOfCleansing());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(thug.getId());

        harness.activateAbility(player1, 0, null, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Spineless Thug");
        harness.assertNotOnBattlefield(player2, "Spineless Thug");
    }

    @Test
    @DisplayName("Parallax Dementia cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player2, new SpinelessThug());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());

        harness.setHand(player1, List.of(new ParallaxDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Parallax Dementia keeps its fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterOnOpponentsUpkeep() {
        Permanent thug = addCreatureReady(player2, new SpinelessThug());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(thug.getId());
        aura.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Parallax Dementia");
    }

    @Test
    @DisplayName("When protection makes the attachment illegal, Parallax Dementia still destroys the creature")
    void destroysEnchantedCreatureWhenAttachmentBecomesIllegal() {
        Permanent thug = addCreatureReady(player2, new SpinelessThug());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(thug.getId());
        thug.getProtectionFromColorsUntilEndOfTurn().add(CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, thug, CardColor.BLACK)).isTrue();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Parallax Dementia");
        harness.assertInGraveyard(player1, "Parallax Dementia");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Spineless Thug");
        harness.assertInGraveyard(player2, "Spineless Thug");
    }
}
