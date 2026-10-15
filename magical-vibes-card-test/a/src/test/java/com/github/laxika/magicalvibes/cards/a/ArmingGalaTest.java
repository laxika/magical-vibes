package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmingGala.class, FutureSight.class, GrizzlyBears.class, Zombify.class})
class ArmingGalaTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, perpetually boosts your creatures and creature cards in every specified zone")
    void boostsCreaturesAndCreatureCardsInAllSpecifiedZones() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ArmingGala());
        harness.addToBattlefield(player1, new FutureSight());

        Card handCreature = new GrizzlyBears();
        Card libraryCreature = new GrizzlyBears();
        Card graveyardCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(handCreature));
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setGraveyard(player1, List.of(graveyardCreature));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        prepareMainPhase();
        addBearMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(handCreature))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanentForCard(handCreature))).isEqualTo(3);

        prepareMainPhase();
        addBearMana();
        harness.castAndResolveFromLibraryTop(player1);
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(libraryCreature))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanentForCard(libraryCreature))).isEqualTo(3);

        prepareMainPhase();
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, graveyardCreature.getId());
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(graveyardCreature))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanentForCard(graveyardCreature))).isEqualTo(3);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addBearMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
