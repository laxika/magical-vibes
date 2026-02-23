package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnderworldDreamsTest extends BaseCardTest {


    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Underworld Dreams has correct card properties")
    void hasCorrectProperties() {
        UnderworldDreams card = new UnderworldDreams();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DRAWS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DRAWS).getFirst())
                .isInstanceOf(DealDamageToTargetPlayerEffect.class);
        DealDamageToTargetPlayerEffect effect =
                (DealDamageToTargetPlayerEffect) card.getEffects(EffectSlot.ON_OPPONENT_DRAWS).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent draw step draw causes 1 life loss")
    void triggersOnOpponentDrawStepDraw() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Underworld Dreams trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Controller draw step draw does not trigger Underworld Dreams")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player1, 20);

        advanceToDraw(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent drawing two cards from a spell loses 2 life")
    void triggersPerCardDrawnFromSpell() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve Counsel of the Soratami
        harness.passBothPriorities(); // resolve first Underworld Dreams trigger
        harness.passBothPriorities(); // resolve second Underworld Dreams trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Underworld Dreams cause 2 damage per opponent draw")
    void twoUnderworldDreamsStack() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Underworld Dreams draw-trigger damage is redirected by Pariah")
    void damageIsRedirectedByPariah() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        Permanent enchantedCreature = new Permanent(new GrizzlyBears());
        enchantedCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(enchantedCreature);

        Permanent pariah = new Permanent(new Pariah());
        pariah.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariah);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Underworld Dreams trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("redirected Underworld Dreams damage"));
    }
}
