package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingBrontodon.class, Forest.class, Island.class})
class RampagingBrontodonTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each land you control when it attacks")
    void boostsForControlledLands() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(9);
    }

    @Test
    @DisplayName("Does not count lands controlled by an opponent")
    void countsOnlyControlledLands() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(7);
    }

    @Test
    @DisplayName("The attack boost lasts until end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Counts lands when the attack trigger resolves")
    void countsLandsAtResolution() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        harness.addToBattlefield(player1, new Island());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(9);
    }

    @Test
    @DisplayName("Does not count a land that leaves before the attack trigger resolves")
    void excludesLandRemovedBeforeResolution() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(7);
    }

    @Test
    @DisplayName("The resolved boost stays fixed when the land count changes")
    void resolvedBoostDoesNotRecalculate() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(8);

        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(8);
    }

    @Test
    @DisplayName("An attack trigger cannot boost the source after it leaves and returns")
    void returnedSourceIsANewPermanent() {
        RampagingBrontodon card = new RampagingBrontodon();
        Permanent original = addCreatureReady(player1, card);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(card);
        gd.playerGraveyards.get(player1.getId()).remove(card);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack boost increases damage that can trample over a blocker")
    void boostedPowerTramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new RampagingBrontodon());
        Permanent blocker = addCreatureReady(player2, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 7, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Rampaging Brontodon");
        harness.assertOnBattlefield(player1, "Rampaging Brontodon");
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
    }
}
