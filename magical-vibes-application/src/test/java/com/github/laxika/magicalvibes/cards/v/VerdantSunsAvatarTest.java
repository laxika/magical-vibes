package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdantSunsAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Verdant Sun's Avatar has effects on both ETB slots")
    void hasCorrectEffectStructure() {
        VerdantSunsAvatar card = new VerdantSunsAvatar();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeEqualToToughnessEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeEqualToToughnessEffect.class);
    }

    @Test
    @DisplayName("Casting Verdant Sun's Avatar puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VerdantSunsAvatar()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Verdant Sun's Avatar");
    }

    @Test
    @DisplayName("Avatar entering triggers self life gain equal to its own toughness (5)")
    void selfEntryTriggersLifeGain() {
        harness.setHand(player1, List.of(new VerdantSunsAvatar()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        // Resolve creature spell → self ETB trigger on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Verdant Sun's Avatar");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) trigger.getEffectsToResolve().getFirst()).amount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolving self-ETB trigger gains 5 life")
    void selfEntryGainsFiveLife() {
        harness.setHand(player1, List.of(new VerdantSunsAvatar()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        // Resolve creature spell → trigger on stack
        harness.passBothPriorities();
        // Resolve trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Another creature entering triggers Avatar's life gain equal to that creature's toughness")
    void anotherCreatureEnteringTriggersLifeGain() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Giant Spider (2/4)
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        // Resolve creature spell → Avatar trigger on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Verdant Sun's Avatar");
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) trigger.getEffectsToResolve().getFirst()).amount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Another creature entering resolves and gains life equal to its toughness")
    void anotherCreatureGainsCorrectLife() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Grizzly Bears (2/2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → trigger on stack
        harness.passBothPriorities();
        // Resolve trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        // Started at 20, gained 2 life (Grizzly Bears toughness = 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Avatar does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        // Avatar on player2's battlefield
        harness.addToBattlefield(player2, new VerdantSunsAvatar());

        // Player1 casts a creature — player2's Avatar should not trigger
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Verdant Sun's Avatars trigger separately for a creature entering")
    void twoAvatarsTriggerSeparately() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Grizzly Bears (2/2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → two triggers on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve first trigger
        harness.passBothPriorities();
        // Resolve second trigger
        harness.passBothPriorities();

        // Started at 20, gained 2 + 2 = 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }
}
