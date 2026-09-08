package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SpireMechcycle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BroodheartEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of its controller's upkeep")
    void upkeepSurveilsOne() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Sacrificing it returns a creature from the graveyard")
    void sacrificesAndReturnsCreature() {
        addReadyEngine(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Broodheart Engine");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can return a Vehicle from the graveyard")
    void returnsVehicle() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spire Mechcycle");
        harness.assertNotInGraveyard(player1, "Spire Mechcycle");
    }

    @Test
    @DisplayName("The ability cannot choose a noncreature non-Vehicle card")
    void cannotChooseInvalidGraveyardCard() {
        addReadyEngine(player1);
        Card noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card must be a creature or Vehicle card");
    }

    private Permanent addReadyEngine(Player player) {
        Permanent engine = new Permanent(new BroodheartEngine());
        engine.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(engine);
        return engine;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
