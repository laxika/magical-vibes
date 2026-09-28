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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == secondCard);
        assertThat(gd.getCardsExiledByPermanent(mirror.getId())).isEmpty();
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

    private Permanent castMirror() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card mirrorCard = new MirrorOfLifeTrapping();
        harness.castFromHand(player1, mirrorCard, "{4}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == mirrorCard)
                .findFirst()
                .orElseThrow();
    }

    private void castCreature(Card creature) {
        harness.castFromHand(player1, creature, "{1}{G}");
        harness.passBothPriorities();
    }
}
