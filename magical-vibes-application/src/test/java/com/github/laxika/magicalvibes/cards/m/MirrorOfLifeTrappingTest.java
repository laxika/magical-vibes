package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorOfLifeTrapping.class, GrizzlyBears.class})
class MirrorOfLifeTrappingTest extends BaseCardTest {

    @Test
    void exilesCastCreaturesAndReturnsPreviouslyExiledCreatures() {
        Permanent mirror = castMirror();
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();

        castCreature(firstCard);
        assertThat(gd.findExiledCard(firstCard.getId()).sourcePermanentId())
                .isEqualTo(mirror.getId());

        castCreature(secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstCard);
        assertThat(gd.findExiledCard(secondCard.getId()).sourcePermanentId())
                .isEqualTo(mirror.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mirror));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == secondCard);
        assertThat(gd.findExiledCard(secondCard.getId())).isNotNull();
    }

    @Test
    void doesNotExileCreatureThatEnteredWithoutBeingCast() {
        castMirror();
        Card creature = new GrizzlyBears();

        harness.enterBattlefieldAndReturn(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.findExiledCard(creature.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsPreviouslyExiledCreatureEvenWhenEnteringCreatureLeavesBeforeResolution() {
        castMirror();
        Card firstCard = new GrizzlyBears();
        castCreature(firstCard);
        Card secondCard = new GrizzlyBears();
        harness.castFromHand(player1, secondCard, "{1}{G}");
        harness.passBothPriorities();
        Permanent entering = findPermanent(player1, "Grizzly Bears");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, entering));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstCard);
        assertThat(gd.findExiledCard(firstCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    void pendingTriggerReturnsOldCardsAfterMirrorLeavesAndKeepsNewCreatureExiled() {
        Permanent mirror = castMirror();
        Card firstCard = new GrizzlyBears();
        castCreature(firstCard);
        Card secondCard = new GrizzlyBears();
        harness.castFromHand(player1, secondCard, "{1}{G}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mirror));
        assertThat(gd.findExiledCard(firstCard.getId())).isNotNull();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstCard)
                .noneMatch(permanent -> permanent.getCard() == secondCard);
        assertThat(gd.findExiledCard(secondCard.getId())).isNotNull();
    }

    @Test
    void exilesOpponentsCastCreatureAndReturnsItToItsOwner() {
        Permanent mirror = castMirror();
        Card opposingCard = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, opposingCard, "{1}{G}");
        resolveAllTriggers();
        assertThat(gd.findExiledCard(opposingCard.getId()).sourcePermanentId())
                .isEqualTo(mirror.getId());

        harness.forceActivePlayer(player1);
        castCreature(new GrizzlyBears());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == opposingCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == opposingCard);
    }
    private Permanent castMirror() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card mirrorCard = new MirrorOfLifeTrapping();
        harness.castFromHand(player1, mirrorCard, "{4}");
        harness.passBothPriorities();
        return findPermanent(player1, "Mirror of Life Trapping");
    }

    private void castCreature(Card creature) {
        harness.castFromHand(player1, creature, "{1}{G}");
        resolveAllTriggers();
    }
}
