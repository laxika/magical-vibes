package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Doomskar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlrundGodOfTheCosmosTest extends BaseCardTest {

    @Test
    void getsPowerAndToughnessForCardsInHandAndForetoldCardsInExile() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        Doomskar foretold = new Doomskar();
        harness.setHand(player1, List.of(new Forest(), new Island(), new GrizzlyBears(), foretold));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(5);

        harness.foretell(player1, 3);

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(5);
    }

    @Test
    void choosesCardTypeAndPutsMatchingRevealedCardsIntoHand() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alrund);
    }

    @Test
    void hakkaReturnsToHandAndScriesAfterCombatDamage() {
        AlrundGodOfTheCosmos card = new AlrundGodOfTheCosmos();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent hakka = findPermanent(player1, "Hakka, Whispering Raven");
        hakka.setSummoningSick(false);
        Card top = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hakka)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(returnedCard -> returnedCard.getId().equals(card.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second, third);
    }
}
