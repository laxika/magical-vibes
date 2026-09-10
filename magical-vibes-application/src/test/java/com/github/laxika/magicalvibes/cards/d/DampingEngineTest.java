package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.f.ForbiddingWatchtower;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.h.HarshMentor;
import com.github.laxika.magicalvibes.cards.k.Knighthood;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DampingEngine.class, ForbiddingWatchtower.class, GiantCockroach.class, Knighthood.class,
        Bloodbriar.class, HarshMentor.class})
class DampingEngineTest extends BaseCardTest {

    @Test
    void specialActionTriggersSacrificeButNotAbilityActivation() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DampingEngine());
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new HarshMentor());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(engine.isDampingEngineEffectIgnoredThisTurn()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void specialActionIsAvailableWhenActivatedAbilitiesAreProhibited() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DampingEngine());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        gd.playersCantActivateAbilitiesThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(engine.isDampingEngineEffectIgnoredThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Player with more permanents than every other player cannot cast a creature spell")
    void leaderCannotCastCreatureSpell() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.setHand(player2, List.of(new GiantCockroach()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Player with more permanents than every other player cannot play a land")
    void leaderCannotPlayLand() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.setHand(player2, List.of(new ForbiddingWatchtower()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The restriction does not apply when permanent counts are tied")
    void equalPermanentCountsAreAllowed() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.setHand(player2, List.of(new GiantCockroach()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Player with more permanents than every other player cannot cast artifact or enchantment spells")
    void leaderCannotCastArtifactOrEnchantmentSpell() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new GiantCockroach());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();

        harness.setHand(player2, List.of(new DampingEngine()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        harness.setHand(player2, List.of(new Knighthood()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice ability cannot be activated when permanent counts are tied")
    void sacrificeAbilityRequiresMorePermanents() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more permanents");
    }

    @Test
    @DisplayName("The controller may sacrifice a permanent to ignore Damping Engine until end of turn")
    void sacrificePermanentIgnoresRestriction() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());

        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof DampingEngine)
                .anyMatch(p -> p.getCard() instanceof GiantCockroach);
    }

    @Test
    @DisplayName("The sacrifice action takes effect without using the stack")
    void sacrificeActionIsImmediate() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("The sacrifice ability can be activated only once each turn")
    void sacrificeAbilityCanBeActivatedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    @Test
    @DisplayName("The affected player may activate Damping Engine's sacrifice ability")
    void affectedPlayerMayActivateSacrificeAbility() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.setHand(player2, List.of(new GiantCockroach()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, sacrificeTarget.getId());

        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("A Damping Engine that loses all abilities does not restrict spells")
    void losingAllAbilitiesRemovesRestriction() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DampingEngine());
        engine.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.setHand(player1, List.of(new Knighthood()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("A face-down Damping Engine does not restrict spells")
    void faceDownEngineDoesNotRestrictSpells() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DampingEngine());
        engine.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.setHand(player1, List.of(new Knighthood()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("A player may sacrifice the Damping Engine itself")
    void controllerMaySacrificeDampingEngineItself() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DampingEngine());
        harness.addToBattlefield(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, engine.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Damping Engine");
        harness.assertInGraveyard(player1, "Damping Engine");
    }

    @Test
    @DisplayName("Ignoring one Damping Engine does not ignore another")
    void ignoringOneEngineDoesNotIgnoreAnother() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new ForbiddingWatchtower());

        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }
}
