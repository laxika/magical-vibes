package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HideousFleshwheeler;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        NezumiFreewheeler.class,
        HideousFleshwheeler.class,
        Forest.class,
        GrizzlyBears.class,
        Shock.class
})
class NezumiFreewheelerTest extends BaseCardTest {

    @Test
    void entersAndMakesEachPlayerMillThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new NezumiFreewheeler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void transformsByPayingPhyrexianManaWithLife() {
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        assertThat(freewheeler.isTransformed()).isTrue();
        assertThat(freewheeler.getCard()).isInstanceOf(HideousFleshwheeler.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedTriggerReturnsTargetPermanentCardFromAnyGraveyard() {
        Card target = new GrizzlyBears();
        Card nonPermanent = new Shock();
        harness.setGraveyard(player1, List.of(nonPermanent));
        harness.setGraveyard(player2, List.of(target));
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(freewheeler.isTransformed()).isTrue();
    }

    private Permanent addReadyFreewheeler(Player player) {
        NezumiFreewheeler card = new NezumiFreewheeler();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
