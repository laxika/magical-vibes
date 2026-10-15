package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MurasaBehemoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapThePast.class, MurasaBehemoth.class, Regrowth.class, RakshasaVizier.class})
class ReapThePastTest extends BaseCardTest {

    @Test
    void returnsXRandomCardsAndExilesTheSpell() {
        List<Card> graveyardCards = List.of(new MurasaBehemoth(), new MurasaBehemoth(), new MurasaBehemoth());
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, graveyardCards);
        harness.setHand(player1, List.of(reapThePast));
        addManaForXTwo();

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(graveyardCards).contains(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    @Test
    void returnsNoCardsForZeroXAndStillExilesTheSpell() {
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, List.of(new MurasaBehemoth()));
        harness.setHand(player1, List.of(reapThePast));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    void returnsAllAvailableCardsWhenXExceedsGraveyardSize() {
        Card creature = new MurasaBehemoth();
        Card sorcery = new Regrowth();
        Card opponentCard = new MurasaBehemoth();
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(reapThePast));
        addManaForXTwo();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    @Test
    void resolvesWithEmptyGraveyardAndStillExilesTheSpell() {
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(reapThePast));
        addManaForXTwo();

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    @Test
    void returningCardsToHandDoesNotTriggerGraveyardExileAbilities() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        Card creature = new MurasaBehemoth();
        Card sorcery = new Regrowth();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setHand(player1, List.of(new ReapThePast()));
        addManaForXTwo();

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, sorcery);
        assertThat(gd.stack).isEmpty();
        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }
}
