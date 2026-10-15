package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.Electropotence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinousMinotaur.class, Forest.class, Mountain.class, GrizzlyBears.class, Electropotence.class})
class RuinousMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("When it deals combat damage to an opponent, its controller sacrifices a land")
    void combatDamageToOpponentSacrificesLand() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        addCreatureReady(player1, new GrizzlyBears());
        minotaur.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), mountain.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage to a creature does not trigger the land sacrifice")
    void damageToCreatureDoesNotTrigger() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        minotaur.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage sacrifices the controller's only land, not an opponent's land")
    void combatDamageWithOneLandSacrificesOnlyControllersLand() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        minotaur.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Having no lands does not prevent damage or sacrifice an opponent's land")
    void combatDamageWithoutLandsResolvesWithoutSacrifice() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        harness.addToBattlefield(player2, new Forest());
        minotaur.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Ruinous Minotaur");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also requires a land sacrifice")
    void noncombatDamageToOpponentSacrificesLand() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RuinousMinotaur()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to its own controller does not trigger a land sacrifice")
    void noncombatDamageToControllerDoesNotSacrificeLand() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RuinousMinotaur()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
