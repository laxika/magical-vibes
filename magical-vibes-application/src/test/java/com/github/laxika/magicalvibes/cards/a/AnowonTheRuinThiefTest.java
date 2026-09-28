package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnowonTheRuinThief.class})
class AnowonTheRuinThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Other Rogues you control get +1/+1")
    void boostsOtherRoguesYouControl() {
        Permanent anowon = addReadyPermanent(player1, new AnowonTheRuinThief());
        Permanent rogue = addReadyPermanent(player1, creature("Rogue", 1, 1, CardSubtype.ROGUE));
        Permanent beast = addReadyPermanent(player1, creature("Beast", 2, 2, CardSubtype.BEAST));
        Permanent opposingRogue = addReadyPermanent(player2, creature("Opposing Rogue", 1, 1, CardSubtype.ROGUE));

        assertThat(gqs.computeStaticBonus(gd, rogue).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, rogue).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, anowon).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, beast).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opposingRogue).power()).isZero();
    }

    @Test
    @DisplayName("One or more Rogues mill damage dealt and draw for creatures milled")
    void roguesMillAndDrawForCreaturesMilled() {
        addReadyPermanent(player1, new AnowonTheRuinThief());
        Permanent firstRogue = addReadyPermanent(player1, creature("First Rogue", 1, 1, CardSubtype.ROGUE));
        Permanent secondRogue = addReadyPermanent(player1, creature("Second Rogue", 1, 1, CardSubtype.ROGUE));
        firstRogue.setAttacking(true);
        secondRogue.setAttacking(true);

        setDeck(player1, land("Draw one"), land("Draw two"));
        setDeck(player2,
                creature("Milled Creature One", 1, 1),
                land("Milled Land One"),
                creature("Milled Creature Two", 1, 1),
                land("Milled Land Two"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Rogues do not draw when no creature card is milled")
    void noDrawWithoutCreatureMilled() {
        addReadyPermanent(player1, new AnowonTheRuinThief());
        Permanent rogue = addReadyPermanent(player1, creature("Rogue", 1, 1, CardSubtype.ROGUE));
        rogue.setAttacking(true);

        setDeck(player1, land("Draw card"));
        setDeck(player2, land("Top card"), land("Second card"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A non-Rogue does not trigger Anowon's mill ability")
    void nonRogueDoesNotTrigger() {
        addReadyPermanent(player1, new AnowonTheRuinThief());
        Permanent beast = addReadyPermanent(player1, creature("Beast", 2, 2, CardSubtype.BEAST));
        beast.setAttacking(true);
        setDeck(player2, land("Top card"), land("Second card"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setDeck(Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private static Card creature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card land(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }
}
