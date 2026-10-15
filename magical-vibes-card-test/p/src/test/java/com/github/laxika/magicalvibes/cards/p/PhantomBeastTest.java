package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomBeast.class, GiantGrowth.class, LightningBolt.class, ProdigalPyromancer.class,
        Pacifism.class, Pyroclasm.class, AetherAdept.class})
class PhantomBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent phantomPerm = addCreatureReady(player1, new PhantomBeast());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, phantomPerm.getId());

        // Stack should have Lightning Bolt + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed
        harness.assertNotOnBattlefield(player1, "Phantom Beast");
        harness.assertInGraveyard(player1, "Phantom Beast");
    }

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent phantomPerm = addCreatureReady(player1, new PhantomBeast());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, phantomPerm.getId());

        // Stack should have Giant Growth + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed even when targeted by own controller
        harness.assertNotOnBattlefield(player1, "Phantom Beast");
        harness.assertInGraveyard(player1, "Phantom Beast");
    }

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent phantomPerm = addCreatureReady(player1, new PhantomBeast());

        // Use Prodigal Pyromancer to target Phantom Beast with an activated ability
        Permanent pyroPerm = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyroPerm),
                null, phantomPerm.getId());

        // Stack should have Prodigal Pyromancer's ability + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed
        harness.assertNotOnBattlefield(player1, "Phantom Beast");
        harness.assertInGraveyard(player1, "Phantom Beast");
    }

    @Test
    @DisplayName("Targeting Phantom Beast with a triggered ability sacrifices it before the ability resolves")
    void sacrificedWhenTargetedByTriggeredAbility() {
        Permanent beast = harness.addToBattlefieldAndReturn(player2, new PhantomBeast());
        harness.setHand(player1, List.of(new AetherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, beast.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player2, "Phantom Beast");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Phantom Beast");
        harness.assertInGraveyard(player2, "Phantom Beast");

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c instanceof PhantomBeast);
        harness.assertOnBattlefield(player1, "Aether Adept");
    }

    @Test
    @DisplayName("An Aura spell triggers sacrifice before it can attach")
    void sacrificedBeforeAuraResolves() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new PhantomBeast());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, beast.getId());

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Phantom Beast");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Phantom Beast");
        harness.assertNotOnBattlefield(player1, "Phantom Beast");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Pacifism");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage from a spell without targets does not trigger sacrifice")
    void survivesUntargetedDamage() {
        harness.addToBattlefield(player1, new PhantomBeast());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Phantom Beast");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c instanceof PhantomBeast);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting one Phantom Beast does not sacrifice another")
    void onlyTargetedBeastIsSacrificed() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new PhantomBeast());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PhantomBeast());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, targeted.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(targeted);
        harness.assertInGraveyard(player1, "Phantom Beast");

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }
}
