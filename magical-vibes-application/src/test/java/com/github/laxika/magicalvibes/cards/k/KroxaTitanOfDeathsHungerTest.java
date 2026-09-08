package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KroxaTitanOfDeathsHunger.class, GrizzlyBears.class, Island.class})
class KroxaTitanOfDeathsHungerTest extends BaseCardTest {

    @Test
    void castFromHandSacrificesKroxa() {
        castFromHandWithOpponentHand(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof KroxaTitanOfDeathsHunger);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KroxaTitanOfDeathsHunger);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentLosesLifeAfterDiscardingALand() {
        castFromHandWithOpponentHand(List.of(new Island()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentDoesNotLoseLifeAfterDiscardingANonland() {
        castFromHandWithOpponentHand(List.of(new GrizzlyBears()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void escapedKroxaStaysOnTheBattlefield() {
        KroxaTitanOfDeathsHunger kroxa = new KroxaTitanOfDeathsHunger();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(kroxa);
        graveyard.addAll(IntStream.range(0, 5).mapToObj(ignored -> new GrizzlyBears()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 5).boxed().toList());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escapedKroxa = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(escapedKroxa.getCard()).isInstanceOf(KroxaTitanOfDeathsHunger.class);
        assertThat(escapedKroxa.isEscaped()).isTrue();
    }

    @Test
    void attackingKroxaMakesOpponentDiscard() {
        addCreatureReady(player1, new KroxaTitanOfDeathsHunger());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    private void castFromHandWithOpponentHand(List<Card> opponentHand) {
        harness.setHand(player1, List.of(new KroxaTitanOfDeathsHunger()));
        harness.setHand(player2, new ArrayList<>(opponentHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player2, 0);
        }
    }
}
