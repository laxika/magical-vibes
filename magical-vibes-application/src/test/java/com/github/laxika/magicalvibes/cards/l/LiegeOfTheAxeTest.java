package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LiegeOfTheAxe.class)
class LiegeOfTheAxeTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenAttacking() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheAxe());

        declareAttackers(List.of(0));

        assertThat(liege.isTapped()).isFalse();
    }

    @Test
    void turningFaceUpUntapsIt() {
        harness.setHand(player1, List.of(new LiegeOfTheAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent liege = findPermanent(player1, "Liege of the Axe");
        assertThat(liege.isFaceDown()).isTrue();

        liege.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liege));
        harness.passBothPriorities();

        assertThat(liege.isFaceDown()).isFalse();
        assertThat(liege.isTapped()).isFalse();
    }

    @Test
    void faceDownLiegeTapsWhenAttacking() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheAxe());
        liege.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackers(List.of(0));

        assertThat(liege.isTapped()).isTrue();
    }

    @Test
    void untapWaitsForTriggerResolutionAndOnlyUntapsItsSource() {
        Permanent otherLiege = addCreatureReady(player1, new LiegeOfTheAxe());
        Permanent opposingLiege = addCreatureReady(player2, new LiegeOfTheAxe());
        Permanent liege = addCreatureReady(player1, new LiegeOfTheAxe());
        liege.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        otherLiege.tap();
        opposingLiege.tap();
        liege.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liege));

        assertThat(liege.isFaceDown()).isFalse();
        assertThat(liege.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(liege.isTapped()).isFalse();
        assertThat(otherLiege.isTapped()).isTrue();
        assertThat(opposingLiege.isTapped()).isTrue();
    }
}
