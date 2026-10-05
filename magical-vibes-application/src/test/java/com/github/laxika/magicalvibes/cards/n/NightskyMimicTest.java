package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EdgeOfTheDivinity;
import com.github.laxika.magicalvibes.cards.s.SmolderingButcher;
import com.github.laxika.magicalvibes.cards.s.SutureSpirit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightskyMimic.class, SutureSpirit.class, SmolderingButcher.class, EdgeOfTheDivinity.class})
class NightskyMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a white-and-black spell makes the Mimic 4/4 with flying")
    void whiteBlackSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new NightskyMimic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(4);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.setHand(player1, List.of(new NightskyMimic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(4);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-white spell does not trigger the Mimic")
    void monoWhiteSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.castFromHand(player1, new SutureSpirit(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-black spell does not trigger the Mimic")
    void monoBlackSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.castFromHand(player1, new SmolderingButcher(), "{3}{B}");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A hybrid spell paid entirely with black mana still triggers")
    void hybridSpellTriggersRegardlessOfManaSpent() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.setHand(player1, List.of(new NightskyMimic()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's white-and-black spell does not trigger")
    void opponentsSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new NightskyMimic()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Mimic does not trigger from its own casting before entering")
    void castingMimicDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new NightskyMimic()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent mimic = findPermanent(player1, "Nightsky Mimic");
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A noncreature hybrid spell triggers and its static boost survives the base change")
    void auraBoostAppliesAboveNewBasePowerAndToughness() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());
        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, mimic.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isTrue();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A second qualifying spell triggers again without adding another power boost")
    void secondQualifyingSpellTriggersAgain() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());
        harness.setHand(player1, List.of(new EdgeOfTheDivinity(), new NightskyMimic()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, mimic.getId());
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isTrue();
    }
}
