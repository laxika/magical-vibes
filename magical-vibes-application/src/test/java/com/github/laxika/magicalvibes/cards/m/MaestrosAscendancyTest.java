package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MaestrosAscendancy.class, GrizzlyBears.class, Shock.class, Strangle.class, BonecrusherGiant.class, Stomp.class})
class MaestrosAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the graveyard by sacrificing a creature and exiles it")
    void castsInstantBySacrificingCreatureAndExilesIt() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(player1, 0, player2.getId(), List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getCard().getId());
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("Requires a creature and allows only one graveyard cast each turn")
    void requiresCreatureAndIsLimitedToOneCastPerTurn() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).containsExactly(0, 1);

        harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(firstCreature.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast a permanent card from the graveyard")
    void onlyCastsInstantsAndSorceries() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casts a sorcery from the graveyard and exiles it")
    void castsSorcery() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Strangle spell = new Strangle();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(player1, 0, target.getId(), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Strangle");
    }

    @Test
    @DisplayName("Cannot use the permission on an opponent's turn")
    void cannotCastOnOpponentsTurn() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("A spell whose only target is sacrificed as its cost is still exiled")
    void exilesSpellWithNoLegalTarget() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(
                player1, 0, sacrifice.getId(), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casts an Adventure from the graveyard and retains permission to cast its creature face")
    void castsAdventureFromGraveyard() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        BonecrusherGiant spell = new BonecrusherGiant();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bonecrusher Giant");
    }

    @Test
    @DisplayName("Each copy grants a separate use during the turn")
    void multipleCopiesAllowMultipleCasts() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent firstSacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondSacrifice = addCreatureReady(player1, new GrizzlyBears());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(firstSacrifice.getId()));
        harness.passBothPriorities();
        harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(secondSacrifice.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    @DisplayName("Permission does not bypass sorcery timing")
    void cannotCastSorceryDuringUpkeep() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Strangle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, target.getId(), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Strangle");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
