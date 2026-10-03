package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.d.DauthiVoidwalker;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({AnowonTheRuinThief.class, BruvacTheGrandiloquent.class, DauthiVoidwalker.class, Island.class})
class AnowonTheRuinThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Other Rogues you control get +1/+1")
    void boostsOtherRoguesYouControl() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent rogue = addCreatureReady(player1, creature("Rogue", 1, 1, CardSubtype.ROGUE));
        Permanent beast = addCreatureReady(player1, creature("Beast", 2, 2, CardSubtype.BEAST));
        Permanent opposingRogue = addCreatureReady(player2, creature("Opposing Rogue", 1, 1, CardSubtype.ROGUE));

        assertThat(gqs.computeStaticBonus(gd, rogue).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, rogue).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, anowon).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, beast).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opposingRogue).power()).isZero();
    }

    @Test
    @DisplayName("One or more Rogues mill damage dealt and draw for creatures milled")
    void roguesMillAndDrawForCreaturesMilled() {
        addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent firstRogue = addCreatureReady(player1, creature("First Rogue", 1, 1, CardSubtype.ROGUE));
        Permanent secondRogue = addCreatureReady(player1, creature("Second Rogue", 1, 1, CardSubtype.ROGUE));
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
        addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent rogue = addCreatureReady(player1, creature("Rogue", 1, 1, CardSubtype.ROGUE));
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
        addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent beast = addCreatureReady(player1, creature("Beast", 2, 2, CardSubtype.BEAST));
        beast.setAttacking(true);
        setDeck(player2, land("Top card"), land("Second card"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Anowon's own combat damage triggers milling even with a short library")
    void ownDamageMillsRemainingLibraryAndDraws() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        anowon.setAttacking(true);
        AnowonTheRuinThief milledCreature = new AnowonTheRuinThief();
        Island drawnCard = new Island();
        setDeck(player1, drawnCard, new Island());
        setDeck(player2, milledCreature);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milledCreature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
    }

    @Test
    @DisplayName("Non-Rogue combat damage does not increase Anowon's mill count")
    void simultaneousNonRogueDamageIsExcluded() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent bruvac = addCreatureReady(player1, new BruvacTheGrandiloquent());
        anowon.setAttacking(true);
        bruvac.setAttacking(true);
        setDeck(player2, new Island(), new Island(), new Island(), new Island(), new Island(), new Island());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A creature among additional cards milled by Bruvac causes a draw")
    void drawsForCreatureAmongReplacementAddedCards() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        addCreatureReady(player1, new BruvacTheGrandiloquent());
        anowon.setAttacking(true);
        AnowonTheRuinThief milledCreature = new AnowonTheRuinThief();
        Island drawnCard = new Island();
        setDeck(player1, drawnCard, new Island());
        setDeck(player2, new Island(), new Island(), milledCreature, new Island(), new Island());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4).contains(milledCreature);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
    }

    @Test
    @DisplayName("A milled creature exiled by Dauthi Voidwalker still causes a draw")
    void drawsForMilledCreatureDivertedToExile() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        addCreatureReady(player1, new DauthiVoidwalker());
        anowon.setAttacking(true);
        AnowonTheRuinThief milledCreature = new AnowonTheRuinThief();
        Island drawnCard = new Island();
        setDeck(player1, drawnCard, new Island());
        setDeck(player2, milledCreature, new Island(), new Island());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(milledCreature.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
    }

    @Test
    @DisplayName("Anowon does not trigger for an opponent's Rogue")
    void opposingRogueDoesNotTrigger() {
        addCreatureReady(player1, new AnowonTheRuinThief());
        Permanent opposingRogue = addCreatureReady(player2, new DauthiVoidwalker());
        opposingRogue.setAttacking(true);
        setDeck(player1, new Island(), new Island(), new Island(), new Island());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("No card is drawn when the damaged player's library is empty")
    void emptyLibraryDoesNotCauseDraw() {
        Permanent anowon = addCreatureReady(player1, new AnowonTheRuinThief());
        anowon.setAttacking(true);
        setDeck(player1, new Island(), new Island());
        setDeck(player2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
    private void setDeck(Player player, Card... cards) {
        harness.setLibrary(player, List.of(cards));
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
