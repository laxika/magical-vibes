package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandskin.class, BarrenMoor.class, GlorySeeker.class, Shock.class, Sparksmith.class})
class SandskinTest extends BaseCardTest {

    @Test
    @DisplayName("Sandskin attaches to a targeted creature")
    void attachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());

        harness.setHand(player1, List.of(new Sandskin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sandskin")
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Sandskin cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BarrenMoor());
        harness.setHand(player1, List.of(new Sandskin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent land = findPermanent(player1, "Barren Moor");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sandskin prevents combat damage dealt by the enchanted creature")
    void preventsCombatDamageByEnchantedCreature() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Sandskin());
        aura.setAttachedTo(attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sandskin prevents combat damage dealt to the enchanted creature")
    void preventsCombatDamageToEnchantedCreature() {
        addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Sandskin());
        aura.setAttachedTo(blocker.getId());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Sandskin does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Sandskin());
        aura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Sandskin does not prevent noncombat damage dealt by the enchanted creature")
    void doesNotPreventNoncombatDamageByEnchantedCreature() {
        harness.setLife(player1, 20);

        Permanent sparksmith = addCreatureReady(player1, new Sparksmith());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Sandskin());
        aura.setAttachedTo(sparksmith.getId());
        Permanent target = addCreatureReady(player2, new Sparksmith());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sparksmith");
        harness.assertLife(player1, 18);
    }
}
