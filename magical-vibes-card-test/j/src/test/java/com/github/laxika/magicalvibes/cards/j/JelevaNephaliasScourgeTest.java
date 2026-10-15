package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.g.GrixisCharm;
import com.github.laxika.magicalvibes.cards.v.VisionSkeins;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JelevaNephaliasScourge.class, Divination.class, GrizzlyBears.class,
        BalefulStrix.class, GrixisCharm.class, VisionSkeins.class})
class JelevaNephaliasScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield exiles as many cards from each library as mana spent")
    void entersAndExilesCardsBasedOnManaSpent() {
        List<Card> ownLibrary = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> opponentLibrary = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.castFromHand(player1, new JelevaNephaliasScourge(), "{1}{U}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jeleva = findPermanent(player1, "Jeleva, Nephalia's Scourge");
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking offers one exiled instant or sorcery for free")
    void attackingOffersOneExiledInstantOrSorceryForFree() {
        Divination divination = new Divination();
        Card ownCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(divination, ownCreature,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new JelevaNephaliasScourge(), "{1}{U}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jeleva = findPermanent(player1, "Jeleva, Nephalia's Scourge");
        jeleva.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities)
                .extracting(PendingMayAbility::targetCardId)
                .containsExactly(divination.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).contains(ownCreature);
    }

    @Test
    void enteringWithoutBeingCastExilesNothing() {
        BalefulStrix ownCard = new BalefulStrix();
        BalefulStrix opponentCard = new BalefulStrix();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));

        Permanent jeleva = harness.enterBattlefieldAndReturn(player1, new JelevaNephaliasScourge());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void entryTriggerStillExilesFourPerPlayerAfterJelevaLeaves() {
        harness.setLibrary(player1, List.of(new BalefulStrix(), new BalefulStrix(),
                new BalefulStrix(), new BalefulStrix()));
        harness.setLibrary(player2, List.of(new BalefulStrix(), new BalefulStrix(),
                new BalefulStrix(), new BalefulStrix()));
        harness.castFromHand(player1, new JelevaNephaliasScourge(), "{1}{U}{B}{R}");
        harness.passBothPriorities();
        Permanent jeleva = findPermanent(player1, "Jeleva, Nephalia's Scourge");

        bounceJeleva(jeleva);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jeleva, Nephalia's Scourge");
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void attackWithOnlyExiledCreaturesOffersNoSpell() {
        harness.setLibrary(player1, List.of(new BalefulStrix()));
        harness.setLibrary(player2, List.of(new BalefulStrix()));
        Permanent jeleva = castAndResolveJeleva();

        attackWithJeleva(jeleva);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).hasSize(2);
    }

    @Test
    void decliningKeepsTheSpellExiled() {
        VisionSkeins spell = new VisionSkeins();
        harness.setLibrary(player1, List.of(spell));
        harness.setLibrary(player2, List.of());
        Permanent jeleva = castAndResolveJeleva();
        attackWithJeleva(jeleva);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).containsExactly(spell);
        harness.assertNotInGraveyard(player1, "Vision Skeins");
    }

    @Test
    void attackCastsOnlyOneSpellFromMultipleEligibleCards() {
        VisionSkeins first = new VisionSkeins();
        VisionSkeins second = new VisionSkeins();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of());
        Permanent jeleva = castAndResolveJeleva();
        attackWithJeleva(jeleva);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).containsExactly(second);
    }

    @Test
    void opponentsSpellIsCastByAttackerAndReturnsToItsOwnersGraveyard() {
        VisionSkeins spell = new VisionSkeins();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(spell));
        Permanent jeleva = castAndResolveJeleva();
        harness.setLibrary(player1, List.of(new BalefulStrix(), new BalefulStrix()));
        harness.setLibrary(player2, List.of(new BalefulStrix(), new BalefulStrix()));
        attackWithJeleva(jeleva);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vision Skeins");
        harness.assertNotInGraveyard(player1, "Vision Skeins");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).isEmpty();
    }

    @Test
    void attackTriggerStillOffersExiledSpellAfterJelevaLeaves() {
        VisionSkeins spell = new VisionSkeins();
        harness.setLibrary(player1, List.of(spell));
        harness.setLibrary(player2, List.of());
        Permanent jeleva = castAndResolveJeleva();
        attackWithJeleva(jeleva);

        bounceJeleva(jeleva);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(spell.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
    }

    @Test
    void returningJelevaCannotCastCardsExiledDuringItsPreviousExistence() {
        VisionSkeins spell = new VisionSkeins();
        harness.setLibrary(player1, List.of(spell));
        harness.setLibrary(player2, List.of());
        Permanent original = castAndResolveJeleva();
        bounceJeleva(original);

        Card returnedJeleva = gd.playerHands.get(player1.getId()).getFirst();
        harness.castFromHand(player1, returnedJeleva, "{1}{U}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Jeleva, Nephalia's Scourge");
        attackWithJeleva(returned);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(returned.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(original.getId())).containsExactly(spell);
    }

    private Permanent castAndResolveJeleva() {
        harness.castFromHand(player1, new JelevaNephaliasScourge(), "{1}{U}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Jeleva, Nephalia's Scourge");
    }

    private void attackWithJeleva(Permanent jeleva) {
        jeleva.setSummoningSick(false);
        declareAttackers(List.of(0));
    }

    private void bounceJeleva(Permanent jeleva) {
        harness.setHand(player1, List.of(new GrixisCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 0, jeleva.getId());
        harness.passBothPriorities();
    }
}
