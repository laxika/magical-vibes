package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuaTuoHonoredPhysician.class, ForestBear.class, Coercion.class})
class HuaTuoHonoredPhysicianTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature card from the graveyard on top of the library")
    void putsCreatureOnTopOfLibrary() {
        Permanent huaTuo = setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        assertThat(huaTuo.isTapped()).isTrue();
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
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card creature = new ForestBear();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupHuaTuoOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupHuaTuoOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new HuaTuoHonoredPhysician());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can activate during upkeep without sorcery timing")
    void canActivateDuringUpkeep() {
        setupHuaTuoOnMyTurn(TurnStep.UPKEEP);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    @DisplayName("Cannot activate during the beginning of a second combat")
    void cannotActivateDuringSecondCombat() {
        setupHuaTuoOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent huaTuo = setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        huaTuo.setSummoningSick(true);
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(huaTuo.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent huaTuo = setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        huaTuo.tap();
        Card creature = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not return a different creature when the target leaves the graveyard")
    void doesNotReplaceMissingTarget() {
        setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card target = new ForestBear();
        Card otherCreature = new ForestBear();
        Card libraryCard = new Coercion();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves after Hua Tuo leaves the battlefield and preserves library order")
    void resolvesWithoutSource() {
        Permanent huaTuo = setupHuaTuoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card creature = new ForestBear();
        Card first = new Coercion();
        Card second = new ForestBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(huaTuo);
        harness.setGraveyard(player1, List.of(creature, huaTuo.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(huaTuo.getCard());
    }

    private Permanent setupHuaTuoOnMyTurn(TurnStep step) {
        Permanent huaTuo = addCreatureReady(player1, new HuaTuoHonoredPhysician());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        return huaTuo;
    }
}
