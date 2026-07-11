package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuaTuoHonoredPhysicianTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature card from the graveyard on top of the library")
    void putsCreatureOnTopOfLibrary() {
        setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card nonCreature = new Coercion();
        harness.setGraveyard(player1, List.of(nonCreature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupHuaTuoOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupHuaTuoOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new HuaTuoHonoredPhysician());
        findPermanent(player1, "Hua Tuo, Honored Physician").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupHuaTuoOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new HuaTuoHonoredPhysician());
        findPermanent(player1, "Hua Tuo, Honored Physician").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
