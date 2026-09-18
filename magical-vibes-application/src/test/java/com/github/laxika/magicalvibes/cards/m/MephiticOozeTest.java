package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.t.TangleSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephiticOoze.class, DarksteelCitadel.class, TangleSpider.class})
class MephiticOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact its controller controls")
    void getsPowerForControlledArtifacts() {
        Permanent ooze = addCreatureReady(player1, new MephiticOoze());

        assertThat(gqs.getEffectivePower(gd, ooze)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(5);

        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroys a creature it deals combat damage to without allowing regeneration")
    void destroysCreatureDealtCombatDamage() {
        addCreatureReady(player1, new MephiticOoze());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        addCreatureReady(player2, new TangleSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Tangle Spider");
        harness.assertNotOnBattlefield(player2, "Tangle Spider");
    }

    @Test
    @DisplayName("The destruction cannot be replaced by regeneration")
    void destructionCannotBeRegenerated() {
        addCreatureReady(player1, new MephiticOoze());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        Permanent blocker = addCreatureReady(player2, new TangleSpider());
        blocker.setRegenerationShield(1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getRegenerationShield()).isEqualTo(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Tangle Spider");
        harness.assertNotOnBattlefield(player2, "Tangle Spider");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the destruction ability")
    void combatDamageToPlayerDoesNotDestroyCreature() {
        addCreatureReady(player1, new MephiticOoze());
        harness.addToBattlefield(player1, new DarksteelCitadel());
        addCreatureReady(player2, new TangleSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Tangle Spider");
    }
}
