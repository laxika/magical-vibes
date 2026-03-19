package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraspOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Grasp of Darkness has correct effect")
    void hasCorrectEffect() {
        GraspOfDarkness card = new GraspOfDarkness();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostTargetCreatureEffect.class);

        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(-4);
        assertThat(effect.toughnessBoost()).isEqualTo(-4);
    }

    @Test
    @DisplayName("Casting puts Grasp of Darkness on the stack targeting a creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Grasp of Darkness");
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving kills a creature with toughness 4 or less")
    void resolvingKillsSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Grasp of Darkness goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grasp of Darkness"));
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
