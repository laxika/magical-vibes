package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValgavothTerrorEater.class, GrizzlyBears.class, Forest.class, Shock.class})
class ValgavothTerrorEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a card exiled with Valgavoth by paying its mana value in life")
    void castsExiledSpellByPayingManaValueLife() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("May play a land exiled with Valgavoth")
    void playsExiledLand() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card forest = new Forest();
        gd.addToExile(player2.getId(), forest, valgavoth.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Exiles an opponent's card put into their graveyard with Valgavoth")
    void exilesOpponentCardInsteadOfGraveyard() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(valgavoth.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Ward can be paid by sacrificing three nonland permanents")
    void wardCanBePaidBySacrificingThreeNonlands() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, valgavoth.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Valgavoth, Terror Eater");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(valgavoth.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
