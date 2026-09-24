package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({VolcanicTorrent.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class VolcanicTorrentTest extends BaseCardTest {

    @Test
    void cascadesToTheFirstLowerManaValueNonlandCard() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        prepareCaster(List.of(new VolcanicTorrent()));

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch cascade =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(cascade.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void dealsDamageEqualToSpellsCastThisTurnToOpponentsCreaturesAndPlaneswalkers() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentPlaneswalker = addPlaneswalker(player2, 5);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setLibrary(player1, List.of());
        prepareCaster(List.of(new Shock(), new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    private void prepareCaster(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 5);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
