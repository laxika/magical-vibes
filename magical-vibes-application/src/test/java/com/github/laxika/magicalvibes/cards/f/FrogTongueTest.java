package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientTomb;
import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrogTongue.class, BayouDragonfly.class, BloodPet.class, AncientTomb.class})
class FrogTongueTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has reach")
    void enchantedCreatureHasReach() {
        Permanent creature = addCreatureReady(player1, new BloodPet());
        attachFrogTongue(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Reach is lost when Frog Tongue leaves the battlefield")
    void reachLostWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new BloodPet());
        Permanent aura = attachFrogTongue(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Casting Frog Tongue draws a card when it enters")
    void drawsCardOnEnter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BloodPet());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FrogTongue()));
        harness.setLibrary(player1, List.of(new BayouDragonfly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve the Aura
        harness.passBothPriorities(); // resolve the enters trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can block a creature with flying")
    void enchantedCreatureCanBlockFlyer() {
        Permanent blocker = addCreatureReady(player2, new BloodPet());
        attachFrogTongue(player2, blocker);
        Permanent attacker = addCreatureReady(player1, new BayouDragonfly());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Frog Tongue cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AncientTomb());
        harness.setHand(player1, List.of(new FrogTongue()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Frog Tongue fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new BloodPet());
        harness.setHand(player1, List.of(new FrogTongue()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Frog Tongue");
        harness.assertNotOnBattlefield(player1, "Frog Tongue");
    }

    private Permanent attachFrogTongue(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new FrogTongue());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);

        return aura;
    }
}
