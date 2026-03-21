package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightbirdsClutchesTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures can't block this turn")
    void twoTargetsCantBlock() {
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreature(player2);

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target just one creature")
    void canTargetJustOne() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target more than two creatures")
    void cannotTargetMoreThanTwo() {
        Permanent c1 = addReadyCreature(player2);
        Permanent c2 = addReadyCreature(player2);
        Permanent c3 = addReadyCreature(player2);

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
        addReadyCreature(player2); // valid target so spell is playable
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
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreature(player2);

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
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
    }

    @Test
    @DisplayName("Flashback makes target creatures unable to block")
    void flashbackMakesCreaturesUnableToBlock() {
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreature(player2);

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
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Nightbird's Clutches"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
    }

    @Test
    @DisplayName("Flashback pays the flashback cost, not the mana cost")
    void flashbackPaysFlashbackCost() {
        Permanent creature = addReadyCreature(player2);

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
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        // No mana added

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Nightbird's Clutches"));
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
