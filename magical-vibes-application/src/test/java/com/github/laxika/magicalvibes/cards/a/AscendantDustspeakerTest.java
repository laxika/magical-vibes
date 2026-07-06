package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AscendantDustspeakerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== ETB +1/+1 counter =====

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another creature you control")
    void etbPutsCounterOnAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AscendantDustspeaker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a second copy targets another creature you control, not the entering one")
    void etbTargetsAnotherCreatureNotEnteringOne() {
        harness.addToBattlefield(player1, new AscendantDustspeaker());
        harness.setHand(player1, List.of(new AscendantDustspeaker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID firstDustspeakerId = harness.getPermanentId(player1, "Ascendant Dustspeaker");
        gs.playCard(gd, player1, 0, 0, firstDustspeakerId, null);
        harness.passBothPriorities(); // resolve creature

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(firstDustspeakerId);
    }

    @Test
    @DisplayName("Can cast without ETB target when no other creatures you control")
    void canCastWithoutEtbTarget() {
        harness.setHand(player1, List.of(new AscendantDustspeaker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ascendant Dustspeaker"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Beginning of combat graveyard exile =====

    @Test
    @DisplayName("Beginning of combat exiles chosen card from opponent's graveyard")
    void beginningOfCombatExilesFromOpponentGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addToBattlefield(player1, new AscendantDustspeaker());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Beginning of combat can exile noncreature card from graveyard")
    void beginningOfCombatExilesNoncreature() {
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.addToBattlefield(player1, new AscendantDustspeaker());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Beginning of combat can choose zero targets when graveyards are not empty")
    void beginningOfCombatCanChooseZeroTargets() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addToBattlefield(player1, new AscendantDustspeaker());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Beginning of combat triggers with empty graveyards and resolves doing nothing")
    void beginningOfCombatWithEmptyGraveyards() {
        harness.addToBattlefield(player1, new AscendantDustspeaker());

        advanceToCombat(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ascendant Dustspeaker");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addToBattlefield(player1, new AscendantDustspeaker());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
