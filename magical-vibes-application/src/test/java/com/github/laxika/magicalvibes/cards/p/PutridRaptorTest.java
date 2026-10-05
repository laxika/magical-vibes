package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridRaptor.class, ScornfulEgotist.class, ZombieCutthroat.class})
class PutridRaptorTest extends BaseCardTest {

    @Test
    void morphRequiresOnlyDiscardAndTurnsFaceUpImmediately() {
        ZombieCutthroat zombie = new ZombieCutthroat();
        harness.setHand(player1, List.of(new PutridRaptor(), zombie));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        assertThat(raptor.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 0);

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
    }

    @Test
    void cannotTurnFaceUpWithoutDiscardingEvenWithEnoughMana() {
        harness.setHand(player1, List.of(new PutridRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(raptor.isFaceDown()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestedRaptorCanUseItsDiscardMorphCostWithoutMana() {
        ZombieCutthroat zombie = new ZombieCutthroat();
        harness.setHand(player1, List.of(zombie));
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new PutridRaptor());
        raptor.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        raptor.setManifested(true);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 0);

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cloakedRaptorCanUseItsDiscardMorphCostWithoutMana() {
        ZombieCutthroat zombie = new ZombieCutthroat();
        harness.setHand(player1, List.of(zombie));
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new PutridRaptor());
        raptor.setFaceDownAsCloaked();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 0);

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turnsFaceUpByDiscardingAZombie() {
        ScornfulEgotist nonZombie = new ScornfulEgotist();
        ZombieCutthroat zombie = new ZombieCutthroat();
        harness.setHand(player1, List.of(new PutridRaptor(), nonZombie, zombie));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 1);
        harness.passBothPriorities();

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
    }

    @Test
    void cannotTurnFaceUpByDiscardingANonZombie() {
        ScornfulEgotist nonZombie = new ScornfulEgotist();
        harness.setHand(player1, List.of(new PutridRaptor(), nonZombie));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(raptor.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
