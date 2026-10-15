package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.TurnStep;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.g.GreaterSandwurm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BontuTheGlorified.class, DuneBeetle.class, Forest.class, GreaterSandwurm.class})
class BontuTheGlorifiedTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when a creature died under your control this turn")
    void canAttackWhenCreatureDiedUnderYourControl() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BontuTheGlorified());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when no creature died this turn")
    void cannotAttackWhenNoCreatureDied() {
        addCreatureReady(player1, new BontuTheGlorified());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only an opponent's creature died this turn")
    void cannotAttackWhenOnlyOpponentCreatureDied() {
        addCreatureReady(player1, new BontuTheGlorified());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing another creature scries, drains each opponent, and gains life")
    void abilityScriesDrainsAndGains() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BontuTheGlorified());
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Scry 1 is offered; keep the card on top to finish resolution
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        // The other creature was sacrificed
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        harness.assertInGraveyard(player1, "Dune Beetle");

        // Opponent loses 1, controller gains 1
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Cannot activate when Bontu is the only creature (can't sacrifice itself)")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new BontuTheGlorified());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void indestructibleSurvivesLethalCombatDamage() {
        addCreatureReady(player1, new BontuTheGlorified());
        addCreatureReady(player2, new GreaterSandwurm());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Bontu the Glorified");
        harness.assertNotInGraveyard(player1, "Bontu the Glorified");
        harness.assertLife(player1, 20);
    }

    @Test
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new BontuTheGlorified());
        addCreatureReady(player2, new DuneBeetle());
        addCreatureReady(player2, new DuneBeetle());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));
        assertThat(gd.playerBattlefields.get(player2.getId())).allSatisfy(blocker -> {
            assertThat(blocker.isBlocking()).isTrue();
            assertThat(blocker.getBlockingTargetIds())
                    .containsExactly(findPermanent(player1, "Bontu the Glorified").getId());
        });
    }

    @Test
    void cannotBlockWithoutCreatureDeathUnderItsController() {
        addCreatureReady(player1, new BontuTheGlorified());
        addCreatureReady(player2, new DuneBeetle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockAfterCreatureDeathUnderItsController() {
        addCreatureReady(player1, new BontuTheGlorified());
        addCreatureReady(player2, new DuneBeetle());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Bontu the Glorified");
        harness.assertInGraveyard(player2, "Dune Beetle");
    }

    @Test
    void sacrificeEnablesCombatBeforeAbilityResolvesAndEmptyLibraryStillDrains() {
        var bontu = addCreatureReady(player1, new BontuTheGlorified());
        bontu.tap();
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Dune Beetle");
        bontu.untap();
        assertThat(als.canAttack(gd, bontu, player1.getId())).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void canPutScryCardOnBottomBeforeDraining() {
        harness.addToBattlefield(player1, new BontuTheGlorified());
        harness.addToBattlefield(player1, new DuneBeetle());
        var forest = new Forest();
        var beetle = new DuneBeetle();
        harness.setLibrary(player1, List.of(forest, beetle));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(beetle, forest);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
