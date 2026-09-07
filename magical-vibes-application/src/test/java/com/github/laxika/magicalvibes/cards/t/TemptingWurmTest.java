package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptingWurm.class, Forest.class, GrizzlyBears.class, HowlingMine.class, LightningBolt.class})
class TemptingWurmTest extends BaseCardTest {

    @Test
    void eachOpponentMayPutAnyNumberOfPermanentCardsOntoTheBattlefield() {
        TemptingWurm wurm = new TemptingWurm();
        Forest ownLand = new Forest();
        Forest opponentLand = new Forest();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        HowlingMine opponentArtifact = new HowlingMine();
        LightningBolt nonPermanent = new LightningBolt();
        harness.setHand(player1, List.of(wurm, ownLand));
        harness.setHand(player2, List.of(opponentLand, opponentCreature, opponentArtifact, nonPermanent));

        castWurm();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                (PendingInteraction.EachPlayerMayPutCardFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(
                opponentLand.getId(), opponentCreature.getId(), opponentArtifact.getId());
        harness.handleMultipleCardsChosen(player2,
                List.of(opponentLand.getId(), opponentCreature.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Grizzly Bears", "Howling Mine");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningLeavesAllCardsInHand() {
        TemptingWurm wurm = new TemptingWurm();
        Forest ownLand = new Forest();
        Forest opponentLand = new Forest();
        harness.setHand(player1, List.of(wurm, ownLand));
        harness.setHand(player2, List.of(opponentLand));

        castWurm();
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWurm() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
