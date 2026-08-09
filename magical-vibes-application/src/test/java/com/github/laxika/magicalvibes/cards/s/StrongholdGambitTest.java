package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.StrongholdGambit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrongholdGambitTest extends BaseCardTest {


    @Test
    void putsEachLowestManaValueCreatureOntoTheBattlefield() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new SerraAngel()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Serra Angel")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(SerraAngel.class);
    }

    @Test
    void putsAllCreaturesTiedForLowestManaValueOntoTheBattlefield() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void leavesNoncreatureChoicesInTheirOwnersHands() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new Forest()));
        harness.setHand(player2, List.of(new LightningBolt()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(LightningBolt.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castGambit() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.StrongholdGambitCardChoice.class);
    }

    private void choose(com.github.laxika.magicalvibes.model.Player player, int cardIndex) {
        harness.handleCardChosen(player, cardIndex);
    }
}
