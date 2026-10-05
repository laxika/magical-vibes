package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SaguMauler;
import com.github.laxika.magicalvibes.cards.s.ScrollOfFate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KadenaSlinkingSorcerer.class, AinokTracker.class, Forest.class, GrizzlyBears.class,
        SaguMauler.class, ScrollOfFate.class})
class KadenaSlinkingSorcererTest extends BaseCardTest {

    @Test
    void firstFaceDownCreatureSpellEachTurnCostsThreeLess() {
        addKadena();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AinokTracker(), new AinokTracker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void faceUpCreatureSpellDoesNotConsumeFaceDownReduction() {
        addKadena();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new AinokTracker()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void drawsWhenAFaceDownCreatureEntersUnderYourControl() {
        KadenaSlinkingSorcerer kadena = new KadenaSlinkingSorcerer();
        harness.addToBattlefield(player1, kadena);
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void faceDownSpellCastBeforeKadenaEnteredConsumesTheReduction() {
        harness.setHand(player1, List.of(new SaguMauler(), new SaguMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        addKadena();

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsForEveryFaceDownCreatureIncludingTheSecondSpell() {
        addKadena();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new SaguMauler(), new SaguMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void faceUpCreatureEnteringDoesNotDraw() {
        addKadena();
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new SaguMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void opponentsFaceDownCreatureGetsNeitherReductionNorDrawTrigger() {
        addKadena();
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player2, List.of(new SaguMauler()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void turningFaceUpNeitherDrawsNorRestoresTheReduction() {
        addKadena();
        Forest firstDraw = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, remaining));
        harness.setHand(player1, List.of(new SaguMauler(), new SaguMauler()));
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestingANoncreatureDrawsWithoutConsumingTheCastReduction() {
        addKadena();
        harness.addToBattlefield(player1, new ScrollOfFate());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new Forest(), new SaguMauler()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(firstDraw);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void reductionResetsOnANewTurn() {
        addKadena();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new SaguMauler(), new SaguMauler()));
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    private void addKadena() {
        harness.addToBattlefield(player1, new KadenaSlinkingSorcerer());
    }
}
