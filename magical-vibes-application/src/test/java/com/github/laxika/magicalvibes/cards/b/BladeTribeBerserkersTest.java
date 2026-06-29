package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BladeTribeBerserkersTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two metalcraft-conditional ETB effects: boost and haste")
    void hasMetalcraftEffects() {
        BladeTribeBerserkers card = new BladeTribeBerserkers();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD))
                .allSatisfy(e -> assertThat(e).isInstanceOf(MetalcraftConditionalEffect.class));

        MetalcraftConditionalEffect boost =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0);
        assertThat(boost.wrapped()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boostEffect = (BoostSelfEffect) boost.wrapped();
        assertThat(boostEffect.powerBoost()).isEqualTo(3);
        assertThat(boostEffect.toughnessBoost()).isEqualTo(3);

        MetalcraftConditionalEffect haste =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1);
        assertThat(haste.wrapped()).isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== ETB with metalcraft met =====

    @Test
    @DisplayName("ETB triggers when metalcraft is met (3+ artifacts)")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blade-Tribe Berserkers");
    }

    @Test
    @DisplayName("ETB resolves: gets +3/+3 and haste until end of turn")
    void etbGrantsBoostAndHaste() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent berserkers = findBerserkers();

        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(berserkers.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    // ===== ETB without metalcraft =====

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts)")
    void etbDoesNotTriggerWithoutMetalcraft() {
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blade-Tribe Berserkers"));

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbDoesNothingWhenMetalcraftLost() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove artifacts before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook"));

        harness.passBothPriorities(); // resolve ETB trigger — metalcraft no longer met

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    // ===== Bonus behavior =====

    @Test
    @DisplayName("Bonus persists after an artifact is removed post-resolution")
    void bonusPersistsAfterArtifactRemoval() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Remove an artifact after resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook"));

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(berserkers.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Creature enters battlefield regardless of metalcraft")
    void creatureEntersWithoutMetalcraft() {
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blade-Tribe Berserkers"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
    }

    private void castBerserkers() {
        harness.setHand(player1, List.of(new BladeTribeBerserkers()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent findBerserkers() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blade-Tribe Berserkers"))
                .findFirst().orElseThrow();
    }
}
