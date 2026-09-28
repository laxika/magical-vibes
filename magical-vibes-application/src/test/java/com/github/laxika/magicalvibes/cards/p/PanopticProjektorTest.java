package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.b.BasilicaStalker;
import com.github.laxika.magicalvibes.cards.r.RiptideSurvivor;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
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
        AvenLiberator.class, SparkSpray.class})
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

        Permanent morphed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst().orElseThrow();
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

        Permanent survivor = findPermanentForCard(survivorCard);
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

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
