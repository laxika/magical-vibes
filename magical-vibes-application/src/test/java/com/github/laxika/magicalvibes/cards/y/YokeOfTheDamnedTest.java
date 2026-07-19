package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YokeOfTheDamnedTest extends BaseCardTest {

    private boolean onBattlefield(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(name));
    }

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Resolving Yoke of the Damned attaches it to the target creature")
    void resolvingAttachesToCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent giant = findPermanent(player1, "Hill Giant");
        harness.setHand(player1, List.of(new YokeOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Yoke of the Damned")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Yoke of the Damned targeting a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new HillGiant()); // valid target so spell is playable
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = findPermanent(player1, "Swamp");
        harness.setHand(player1, List.of(new YokeOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Death trigger: destroy the enchanted creature =====

    @Test
    @DisplayName("When another creature dies, the enchanted creature is destroyed")
    void anotherCreatureDyingDestroysEnchantedCreature() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3, the enchanted creature
        addYokeAttachedTo(player1, "Hill Giant");
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, dies to Shock

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock — Grizzly Bears dies

        // Yoke's death trigger should now be on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // Resolve the trigger

        assertThat(onBattlefield(player1, "Hill Giant")).isFalse();
    }

    @Test
    @DisplayName("When the enchanted creature itself dies, the orphaned Yoke fizzles harmlessly")
    void enchantedCreatureDyingFizzles() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2, enchanted and dies to Shock
        addYokeAttachedTo(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // The creature and its now-orphaned Aura are both gone; nothing else is affected.
        assertThat(onBattlefield(player1, "Grizzly Bears")).isFalse();
        assertThat(onBattlefield(player1, "Yoke of the Damned")).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yoke of the Damned"));
    }

    // ===== Helpers =====

    private void addYokeAttachedTo(Player owner, String creatureName) {
        Permanent creature = findPermanent(owner, creatureName);
        Permanent aura = new Permanent(new YokeOfTheDamned());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }
}
