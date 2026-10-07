package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.p.ProtectiveBubble;
import com.github.laxika.magicalvibes.cards.w.WanderersTwig;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriclopeanSight.class, GoldmeadowStalwart.class, WanderersTwig.class, ProtectiveBubble.class})
class TriclopeanSightTest extends BaseCardTest {

    // ===== ETB untap =====

    @Test
    @DisplayName("Resolving Triclopean Sight untaps the enchanted creature")
    void resolvingUntapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GoldmeadowStalwart());
        creature.tap();

        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities(); // resolve ETB untap trigger

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== +1/+1 boost =====

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TriclopeanSight());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    // ===== Vigilance =====

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TriclopeanSight());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost and vigilance when Triclopean Sight is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TriclopeanSight());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Triclopean Sight")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WanderersTwig());
        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Triclopean Sight goes to the graveyard if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());

        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Triclopean Sight");
        harness.assertNotOnBattlefield(player1, "Triclopean Sight");
    }

    @Test
    @DisplayName("Vigilance keeps the enchanted creature untapped when it attacks")
    void vigilanceKeepsCreatureUntappedWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new GoldmeadowStalwart());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TriclopeanSight());
        aura.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn and benefits their enchanted creature")
    void flashOnOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Triclopean Sight");
    }

    @Test
    @DisplayName("Untap trigger still resolves if enchanted creature gains shroud")
    void untapTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        creature.tap();
        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ProtectiveBubble());
        bubble.setAttachedTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap trigger follows the Aura's current attachment")
    void untapTriggerFollowsCurrentAttachment() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        original.tap();
        replacement.tap();
        harness.setHand(player1, List.of(new TriclopeanSight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Triclopean Sight");
        aura.setAttachedTo(replacement.getId());
        resolveAllTriggers();

        assertThat(original.isTapped()).isTrue();
        assertThat(replacement.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.VIGILANCE)).isTrue();
    }
}
