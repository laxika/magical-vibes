package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightbirdsClutches.class, GrizzlyBears.class, FountainOfYouth.class})
class NightbirdsClutchesTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures can't block this turn")
    void twoTargetsCantBlock() {
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target just one creature")
    void canTargetJustOne() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target more than two creatures")
    void cannotTargetMoreThanTwo() {
        Permanent c1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent c2 = addCreatureReady(player2, new GrizzlyBears());
        Permanent c3 = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeted creature actually cannot block in combat")
    void targetedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId()));

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));

        // Remove creature1 before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature1);

        harness.passBothPriorities();

        // creature2 should still be affected
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving (normal cast)")
    void goesToGraveyardAfterResolving() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        harness.assertInGraveyard(player1, "Nightbird's Clutches");
    }

    @Test
    @DisplayName("Flashback makes target creatures unable to block")
    void flashbackMakesCreaturesUnableToBlock() {
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Nightbird's Clutches");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
    }

    @Test
    @DisplayName("Flashback pays the flashback cost, not the mana cost")
    void flashbackPaysFlashbackCost() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        // Flashback cost is {3}{R}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        // No mana added

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()));

        harness.assertNotInGraveyard(player1, "Nightbird's Clutches");
    }

    @Test
    @DisplayName("Can resolve with zero targets and no creatures on the battlefield")
    void canCastWithZeroTargets() {
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Nightbird's Clutches");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can flash back with zero targets")
    void canFlashbackWithZeroTargets() {
        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Nightbird's Clutches");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotTargetSameCreatureTwice() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target your own creature without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId()));

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(otherCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction expires before the next turn")
    void blockingRestrictionExpires() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));
        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Flashback exiles the spell even if every target leaves before resolution")
    void flashbackExilesWhenAllTargetsBecomeIllegal() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
        harness.assertNotInGraveyard(player1, "Nightbird's Clutches");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
        assertThat(gd.stack).isEmpty();
    }
}
