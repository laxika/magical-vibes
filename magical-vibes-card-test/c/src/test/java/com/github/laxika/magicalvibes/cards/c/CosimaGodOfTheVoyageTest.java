package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralLongboat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TheOmenkeel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ControlMagic.class, CosimaGodOfTheVoyage.class, TheOmenkeel.class, Forest.class,
        GrizzlyBears.class, SerraAngel.class, FuneralLongboat.class})
class CosimaGodOfTheVoyageTest extends BaseCardTest {

    @Test
    void castingTheBackFaceCreatesACrewableVehicle() {
        harness.setHand(player1, List.of(new CosimaGodOfTheVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent omenkeel = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, omenkeel)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void acceptingVoyageCountersThenDecliningReturnsCosimaAndDraws() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player1, new CosimaGodOfTheVoyage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledVoyageCounters).containsEntry(cosima.getOriginalCard().getId(), 1);

        Card drawCard = new GrizzlyBears();
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(drawCard));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent returnedCosima = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(cosima.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedCosima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
        assertThat(gd.exiledVoyageCounters).doesNotContainKey(cosima.getOriginalCard().getId());
    }

    @Test
    void omenkeelExilesDamageCountAndKeepsLandPermissionAfterLeaving() {
        Permanent omenkeel = addCreatureReady(player1, new TheOmenkeel());
        addCreatureReady(player1, new SerraAngel());
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland, new Forest()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel), null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel)));
        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(omenkeel);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(omenkeel.getId())).hasSize(3);

        gd.playerBattlefields.get(player1.getId()).remove(omenkeel);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, nonland.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void decliningUpkeepExileKeepsCosimaOnTheBattlefield() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player1, new CosimaGodOfTheVoyage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cosima);
        assertThat(gd.findExiledCard(cosima.getOriginalCard().getId())).isNull();
    }

    @Test
    void decliningFirstVoyageCounterReturnsWithoutCountersOrDrawing() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player1, new CosimaGodOfTheVoyage());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Card drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawCard));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent returned = findPermanent(player1, "Cosima, God of the Voyage");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        assertThat(gd.findExiledCard(cosima.getOriginalCard().getId())).isNull();
    }

    @Test
    void exiledStolenCosimaTriggersForItsOwnersLand() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player2, new CosimaGodOfTheVoyage());
        gd.playerBattlefields.get(player2.getId()).remove(cosima);
        gd.playerBattlefields.get(player1.getId()).add(cosima);
        gd.stolenCreatures.put(cosima.getId(), player2.getId());
        harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.c.ControlMagic()).setAttachedTo(cosima.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.findExiledCard(cosima.getOriginalCard().getId()).ownerId()).isEqualTo(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        Card drawCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(drawCard));
        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawCard);
        harness.assertOnBattlefield(player2, "Cosima, God of the Voyage");
        harness.assertNotOnBattlefield(player1, "Cosima, God of the Voyage");
    }

    @Test
    void exiledStolenCosimaDoesNotTriggerForItsFormerControllersLand() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player2, new CosimaGodOfTheVoyage());
        gd.playerBattlefields.get(player2.getId()).remove(cosima);
        gd.playerBattlefields.get(player1.getId()).add(cosima);
        gd.stolenCreatures.put(cosima.getId(), player2.getId());
        harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.c.ControlMagic()).setAttachedTo(cosima.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void leavingExileAndBeingExiledAgainDoesNotRetainVoyageAbility() {
        CosimaGodOfTheVoyage card = new CosimaGodOfTheVoyage();
        harness.addToBattlefield(player1, card);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.removeFromExile(card.getId())).isTrue();
        harness.setGraveyard(player1, List.of(card));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.setExile(player1, List.of(card));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void omenkeelTriggersForAnotherVehicleWhileItIsNotCrewed() {
        Permanent omenkeel = harness.addToBattlefieldAndReturn(player1, new TheOmenkeel());
        Permanent longboat = addCreatureReady(player1, new FuneralLongboat());
        addCreatureReady(player1, new SerraAngel());
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land, new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(longboat), null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(longboat)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(omenkeel.getId())).hasSize(3);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
    }
}
