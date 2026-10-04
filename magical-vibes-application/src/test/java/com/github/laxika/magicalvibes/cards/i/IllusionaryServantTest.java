package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusionaryServant.class, GiantGrowth.class, ProdigalPyromancer.class, LightningBolt.class, Cancel.class, Humility.class})
class IllusionaryServantTest extends BaseCardTest {

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent servantPerm = addCreatureReady(player1, new IllusionaryServant());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, servantPerm.getId());

        // Stack should have Lightning Bolt + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed
        harness.assertNotOnBattlefield(player1, "Illusionary Servant");
        harness.assertInGraveyard(player1, "Illusionary Servant");
    }

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent servantPerm = addCreatureReady(player1, new IllusionaryServant());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, servantPerm.getId());

        // Stack should have Giant Growth + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed even when targeted by own controller
        harness.assertNotOnBattlefield(player1, "Illusionary Servant");
        harness.assertInGraveyard(player1, "Illusionary Servant");
    }

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent servantPerm = addCreatureReady(player1, new IllusionaryServant());

        // Use Prodigal Pyromancer to target Illusionary Servant with an activated ability
        Permanent pyroPerm = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyroPerm),
                null, servantPerm.getId());

        // Stack should have Prodigal Pyromancer's ability + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed
        harness.assertNotOnBattlefield(player1, "Illusionary Servant");
        harness.assertInGraveyard(player1, "Illusionary Servant");
    }

    @Test
    @DisplayName("Sacrifice waits for its trigger to resolve, then the targeting spell has no legal target")
    void sacrificeIsNotImmediateAndTargetingSpellDoesNotResolve() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new IllusionaryServant());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, servant.getId());

        harness.assertOnBattlefield(player1, "Illusionary Servant");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Illusionary Servant");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Countering the targeting spell does not remove the sacrifice trigger")
    void sacrificeStillResolvesAfterTargetingSpellIsCountered() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new IllusionaryServant());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, servant.getId());
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, bolt.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Illusionary Servant");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Illusionary Servant");
        harness.assertNotOnBattlefield(player1, "Illusionary Servant");
    }

    @Test
    @DisplayName("Targeting another player does not trigger Illusionary Servant")
    void noSacrificeWhenSpellTargetsAnotherObject() {
        harness.addToBattlefield(player1, new IllusionaryServant());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player1, "Illusionary Servant");
        harness.assertNotInGraveyard(player1, "Illusionary Servant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Illusionary Servant does not trigger after Humility removes its abilities")
    void noSacrificeWhenPrintedAbilityHasBeenRemoved() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new IllusionaryServant());
        harness.addToBattlefield(player2, new Humility());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, servant.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Illusionary Servant");
        harness.assertNotInGraveyard(player1, "Illusionary Servant");
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(4);
    }
}
