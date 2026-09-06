package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scrapshooter.class, FountainOfYouth.class})
class ScrapshooterTest extends BaseCardTest {

    @Test
    void withoutGiftDoesNotDrawOrDestroy() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(false, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void promisedGiftDrawsAndDestroysOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true, artifact.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void promisedGiftCannotTargetYourOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureWithGift(player1, 0, artifact.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void cast(boolean giftPromised, UUID targetId) {
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();
        harness.castCreatureWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
