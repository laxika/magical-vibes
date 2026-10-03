package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestructiveUrge.class, CoralMerfolk.class, Forest.class, Mountain.class, Disenchant.class})
class DestructiveUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destructive Urge can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new CoralMerfolk());
        DestructiveUrge urge = new DestructiveUrge();

        harness.setHand(player1, List.of(urge));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == urge
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Destructive Urge cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new DestructiveUrge()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, land.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The damaged player chooses a land to sacrifice")
    void damagedPlayerChoosesLandToSacrifice() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);

        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new CoralMerfolk());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(mountain.getId(), forest.getId())
                .doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger resolves without a choice when the damaged player controls no lands")
    void noLandToSacrifice() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);
        addCreatureReady(player2, new CoralMerfolk());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger the land sacrifice")
    void blockedCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addToBattlefield(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("An opponent's enchanted creature makes the Aura controller sacrifice a land when damaged")
    void opponentControlledCreatureDamagesAuraController() {
        Permanent attacker = addCreatureReady(player2, new CoralMerfolk());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        resolveCombat(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(mountain.getId(), forest.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Destroying the Aura after combat damage does not remove its sacrifice trigger")
    void triggerSurvivesAuraDestruction() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);
        Permanent aura = findPermanent(player1, "Destructive Urge");
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Destructive Urge");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void attachDestructiveUrge(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new DestructiveUrge());
        aura.setAttachedTo(creature.getId());
    }
}
