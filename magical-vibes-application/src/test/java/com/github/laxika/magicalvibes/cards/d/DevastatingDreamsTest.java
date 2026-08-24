package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevastatingDreams.class, Forest.class, Mountain.class, GrizzlyBears.class, HillGiant.class})
class DevastatingDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Randomly discards X, sacrifices X lands, and deals X damage to each creature")
    void resolvesAllEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new DevastatingDreams(), new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(giant.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Devastating Dreams");
    }

    @Test
    @DisplayName("Each player chooses which lands to sacrifice")
    void eachPlayerChoosesLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevastatingDreams(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("X=0 does nothing")
    void zeroXDoesNothing() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevastatingDreams(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }
}
