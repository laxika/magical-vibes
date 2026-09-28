package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LobeliaDefenderOfBagEnd.class, FountainOfYouth.class, GrizzlyBears.class})
class LobeliaDefenderOfBagEndTest extends BaseCardTest {

    @Test
    void entersAndExilesEachOpponentsTopCardFaceDown() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new LobeliaDefenderOfBagEnd()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lobelia = findPermanent(player1, "Lobelia, Defender of Bag End");
        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(gd.getCardsExiledByPermanent(lobelia.getId())).containsExactly(topCard);
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void artifactSacrificeModeDrainsEachOpponent() {
        Permanent lobelia = addReadyLobelia();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Each opponent loses 2 life and you gain 2 life");

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    void freePlayModeCastsOneCardExiledWithLobelia() {
        Permanent lobelia = addReadyLobelia();
        harness.addToBattlefield(player1, new FountainOfYouth());
        Card exiledCard = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCard, lobelia.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Until end of turn, you may play a card exiled with Lobelia without paying its mana cost");
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledCard);
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    private Permanent addReadyLobelia() {
        LobeliaDefenderOfBagEnd card = new LobeliaDefenderOfBagEnd();
        Permanent lobelia = new Permanent(card);
        lobelia.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lobelia);
        return lobelia;
    }
}
