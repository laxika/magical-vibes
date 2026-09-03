package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WearDown.class, FountainOfYouth.class, GhostlyPrison.class, GrizzlyBears.class})
class WearDownTest extends BaseCardTest {

    @Test
    void withoutGiftDestroysOneArtifactOrEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(List.of(artifact.getId()), false);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Ghostly Prison");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void withGiftDestroysTwoAndOpponentDraws() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(List.of(artifact.getId(), enchantment.getId()), true);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds, boolean giftPromised) {
        prepareSpell();
        harness.castSorceryWithGift(player1, 0, targetIds, giftPromised);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new WearDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
