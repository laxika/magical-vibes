package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LocketOfYesterdays.class, HillGiant.class, Shock.class, ThinkTwice.class, FathomSeer.class})
class LocketOfYesterdaysTest extends BaseCardTest {

    @Test
    void reducesGenericCostForEachMatchingCardInControllerGraveyard() {
        addLocket();
        harness.setGraveyard(player1, List.of(new HillGiant(), new HillGiant()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionsFromMultipleLocketsStack() {
        addLocket();
        addLocket();
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void onlyUsesTheCastingPlayersGraveyard() {
        addLocket();
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setGraveyard(player2, List.of(new HillGiant(), new HillGiant()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredMana() {
        addLocket();
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCountTheSpellItselfWhenCastFromGraveyard() {
        addLocket();
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesFlashbackCostForOtherMatchingCards() {
        addLocket();
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessReductionDoesNotRemoveColoredRequirement() {
        addLocket();
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice(), new ThinkTwice()));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void excessReductionStopsAtZeroGenericMana() {
        addLocket();
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice(), new ThinkTwice()));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsLocketDoesNotReduceYourSpells() {
        harness.addToBattlefield(player2, new LocketOfYesterdays());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unrelatedGraveyardCardsDoNotReduceCost() {
        addLocket();
        harness.setGraveyard(player1, List.of(new LocketOfYesterdays()));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void faceDownSpellHasNoNameToMatchGraveyardCards() {
        addLocket();
        harness.setGraveyard(player1, List.of(new FathomSeer()));
        harness.setHand(player1, List.of(new FathomSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addLocket() {
        harness.addToBattlefield(player1, new LocketOfYesterdays());
    }
}
