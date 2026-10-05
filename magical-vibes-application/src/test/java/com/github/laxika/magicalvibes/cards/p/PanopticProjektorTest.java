package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.b.BasilicaStalker;
import com.github.laxika.magicalvibes.cards.r.RiptideSurvivor;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PanopticProjektor.class, BasilicaStalker.class, RiptideSurvivor.class,
        AvenLiberator.class, SparkSpray.class, ZoeticCavern.class})
class PanopticProjektorTest extends BaseCardTest {

    @Test
    void reducesTheNextFaceDownCreatureSpellByThree() {
        Permanent projector = harness.addToBattlefieldAndReturn(player1, new PanopticProjektor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        BasilicaStalker card = new BasilicaStalker();
        harness.setHand(player1, List.of(card));
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent morphed = findPermanent(player1, "Basilica Stalker");
        assertThat(morphed.isFaceDown()).isTrue();
        assertThat(projector.isTapped()).isTrue();
    }

    @Test
    void reductionDoesNotApplyToFaceUpCreatureSpells() {
        harness.addToBattlefield(player1, new PanopticProjektor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new BasilicaStalker()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublesTriggeredAbilitiesCausedByTurningAPermanentFaceUp() {
        harness.addToBattlefield(player1, new PanopticProjektor());
        RiptideSurvivor survivorCard = new RiptideSurvivor();
        harness.setHand(player1, List.of(
                survivorCard, new SparkSpray(), new SparkSpray(), new SparkSpray(), new SparkSpray()));
        List<Card> library = List.of(
                new AvenLiberator(), new AvenLiberator(), new AvenLiberator(),
                new AvenLiberator(), new AvenLiberator(), new AvenLiberator());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent survivor = findPermanent(player1, "Riptide Survivor");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            harness.handleCardChosen(player1, 0);
        }
        harness.passBothPriorities();
        for (int i = 0; i < 2; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void faceDownLandSpellConsumesTheDiscount() {
        harness.addToBattlefield(player1, new PanopticProjektor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ZoeticCavern(), new ZoeticCavern()));

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zoetic Cavern").isFaceDown()).isTrue();
        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingAFaceUpCreatureDoesNotConsumeTheDiscount() {
        harness.addToBattlefield(player1, new PanopticProjektor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RiptideSurvivor(), new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zoetic Cavern").isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void twoProjektorsAddTwoTriggersRatherThanDoublingTwice() {
        harness.addToBattlefield(player1, new PanopticProjektor());
        harness.addToBattlefield(player1, new PanopticProjektor());
        harness.setHand(player1, List.of(new RiptideSurvivor(),
                new SparkSpray(), new SparkSpray(), new SparkSpray(), new SparkSpray()));
        harness.setLibrary(player1, List.of(
                new AvenLiberator(), new AvenLiberator(), new AvenLiberator(),
                new AvenLiberator(), new AvenLiberator(), new AvenLiberator(),
                new AvenLiberator(), new AvenLiberator(), new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent survivor = findPermanent(player1, "Riptide Survivor");

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        for (int copy = 0; copy < 3; copy++) {
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDoubleAnOpponentsFaceUpTrigger() {
        harness.addToBattlefield(player2, new PanopticProjektor());
        harness.setHand(player1, List.of(new RiptideSurvivor()));
        harness.setLibrary(player1, List.of(new SparkSpray(), new SparkSpray(), new SparkSpray()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent survivor = findPermanent(player1, "Riptide Survivor");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
