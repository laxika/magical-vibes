package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.ImperiousOligarch;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryOfTheCarnarium.class, GrizzlyBears.class, HillGiant.class, Shock.class, ImperiousOligarch.class})
class CryOfTheCarnariumTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures -2/-2 and exiles creatures reduced to zero toughness")
    void weakensCreaturesAndExilesThoseThatDie() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Card bears = new GrizzlyBears();
        addCreatureReady(player2, bears);
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Exiles only creature cards put into graveyards from the battlefield this turn")
    void exilesOnlyRecentBattlefieldCreatureCards() {
        Card olderCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(olderCreature, noncreature));
        Card recentCreature = new GrizzlyBears();
        Permanent recentPermanent = addCreatureReady(player2, recentCreature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recentPermanent));

        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(recentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(olderCreature, noncreature);
    }

    @Test
    @DisplayName("The replacement effect expires at end of turn")
    void replacementEffectExpiresAtEndOfTurn() {
        Card giantCard = new HillGiant();
        addCreatureReady(player2, giantCard);
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent giant = findPermanent(player2, "Hill Giant");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, giant));

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giantCard);
    }

    @Test
    @DisplayName("Exiling creatures with zero toughness prevents afterlife on both sides")
    void zeroToughnessCreaturesDoNotDie() {
        Card ownCreature = new ImperiousOligarch();
        Card opposingCreature = new ImperiousOligarch();
        addCreatureReady(player1, ownCreature);
        addCreatureReady(player2, opposingCreature);
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering later are not weakened but are still exiled instead of dying")
    void replacementAlsoAppliesToLaterCreatures() {
        harness.setHand(player1, List.of(new CryOfTheCarnarium(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        Card bearsCard = new GrizzlyBears();
        Permanent bears = addCreatureReady(player2, bearsCard);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bearsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bearsCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Recent battlefield creature cards are exiled from the caster's graveyard too")
    void exilesRecentCreatureCardsFromOwnGraveyard() {
        Card recentCreature = new HillGiant();
        Permanent recentPermanent = addCreatureReady(player1, recentCreature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recentPermanent));
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(recentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(recentCreature);
    }

    @Test
    @DisplayName("The power and toughness reduction wears off at end of turn")
    void weakeningExpiresAtEndOfTurn() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }
}
