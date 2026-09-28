package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialKavu.class, Forest.class, Island.class, GrizzlyBears.class})
class TerritorialKavuTest extends BaseCardTest {

    private static final String DISCARD_MODE = "Discard a card. If you do, draw a card.";
    private static final String EXILE_MODE = "Exile up to one target card from a graveyard.";

    @Test
    void powerAndToughnessEqualDomainCount() {
        Permanent kavu = addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    void attackingCanDiscardThenDraw() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));
        addReadyKavu(player1);

        declareAttack();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingCanExileUpToOneCardFromAnyGraveyard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(card));
        addReadyKavu(player1);

        declareAttack();
        harness.handleListChoice(player1, EXILE_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).contains(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    private Permanent addReadyKavu(Player player) {
        Permanent kavu = new Permanent(new TerritorialKavu());
        kavu.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(kavu);
        return kavu;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
