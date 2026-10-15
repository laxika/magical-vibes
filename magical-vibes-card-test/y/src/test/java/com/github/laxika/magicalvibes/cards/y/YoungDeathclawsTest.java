package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoungDeathclaws.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class})
class YoungDeathclawsTest extends BaseCardTest {

    private Permanent addYoungDeathclaws() {
        return addCreatureReady(player1, new YoungDeathclaws());
    }

    @Test
    @DisplayName("Grants scavenge for each creature card's own mana cost")
    void grantsScavengeEqualToManaCost() {
        addYoungDeathclaws();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant scavenge to a noncreature card")
    void doesNotGrantScavengeToNoncreatureCard() {
        addYoungDeathclaws();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without Young Deathclaws on the battlefield, creature cards have no granted scavenge")
    void noScavengeWithoutYoungDeathclaws() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Humble.class)
    @DisplayName("Losing all abilities stops granting scavenge")
    void losingAbilitiesStopsScavengeGrant() {
        Permanent deathclaws = addYoungDeathclaws();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Humble()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, deathclaws.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Face-down Young Deathclaws does not grant scavenge")
    void faceDownDeathclawsDoesNotGrantScavenge() {
        Permanent deathclaws = addYoungDeathclaws();
        deathclaws.setFaceDownAsCloaked();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        prepareDeathclawsScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, deathclaws.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Young Deathclaws");
    }

    @Test
    @DisplayName("Scavenge exiles its source as a cost and can target an opponent's creature")
    void exilesSourceBeforeResolvingOnOpposingCreature() {
        addYoungDeathclaws();
        Permanent target = addCreatureReady(player2, new YoungDeathclaws());
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        prepareDeathclawsScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Young Deathclaws");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Young Deathclaws");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Young Deathclaws does not grant scavenge to the opponent's graveyard")
    void doesNotGrantScavengeToOpponent() {
        Permanent target = addYoungDeathclaws();
        harness.setGraveyard(player2, List.of(new YoungDeathclaws()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge cannot be activated outside a main phase")
    void scavengeRequiresMainPhase() {
        Permanent target = addYoungDeathclaws();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        prepareDeathclawsScavenge();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Young Deathclaws");
    }

    @Test
    @DisplayName("Scavenge cannot be activated while the stack is nonempty")
    void scavengeRequiresEmptyStack() {
        Permanent target = addYoungDeathclaws();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws(), new YoungDeathclaws()));
        prepareDeathclawsScavenge();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Scavenge requires the creature card's colored mana")
    void scavengeCannotBePaidWithOnlyGenericMana() {
        Permanent target = addYoungDeathclaws();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Young Deathclaws");
    }

    @Test
    @DisplayName("Menace rejects one blocker and permits two blockers")
    void menaceRequiresTwoBlockers() {
        addYoungDeathclaws();
        Permanent firstBlocker = addCreatureReady(player2, new YoungDeathclaws());
        Permanent secondBlocker = addCreatureReady(player2, new YoungDeathclaws());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scavenge cannot be activated on the opponent's turn")
    void scavengeRequiresControllersTurn() {
        Permanent target = addYoungDeathclaws();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        prepareDeathclawsScavenge();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Young Deathclaws");
    }

    @Test
    @DisplayName("Scavenge rejects a player target before exiling its source")
    void scavengeRequiresCreatureTarget() {
        addYoungDeathclaws();
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        prepareDeathclawsScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Young Deathclaws");
    }

    @Test
    @DisplayName("An activated scavenge ability resolves after Young Deathclaws leaves the battlefield")
    void activatedScavengeSurvivesGrantingCreatureRemoval() {
        Permanent deathclaws = addYoungDeathclaws();
        Permanent target = addCreatureReady(player2, new YoungDeathclaws());
        harness.setGraveyard(player1, List.of(new YoungDeathclaws()));
        harness.setHand(player1, List.of(new LightningBolt()));
        prepareDeathclawsScavenge();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.castInstant(player1, 0, deathclaws.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Young Deathclaws");
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void prepareDeathclawsScavenge() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
