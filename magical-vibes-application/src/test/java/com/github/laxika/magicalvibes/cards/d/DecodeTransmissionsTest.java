package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecodeTransmissions.class, Forest.class, GrizzlyBears.class})
class DecodeTransmissionsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and makes its controller lose 2 life without Void")
    void drawsTwoAndControllerLosesLifeWithoutVoid() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DecodeTransmissions()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        addMana();

        castDecodeTransmissions();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Void draws two cards and makes each opponent lose 2 life")
    void voidDrawsTwoAndEachOpponentLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.setHand(player1, List.of(new DecodeTransmissions()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        addMana();

        castDecodeTransmissions();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A land leaving the battlefield does not enable Void")
    void landLeavingBattlefieldDoesNotEnableVoid() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.setHand(player1, List.of(new DecodeTransmissions()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        addMana();

        castDecodeTransmissions();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castDecodeTransmissions() {
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
