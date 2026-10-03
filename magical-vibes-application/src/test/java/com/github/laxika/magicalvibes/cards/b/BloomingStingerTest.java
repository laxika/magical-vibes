package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChromeCompanion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloomingStinger.class, ChromeCompanion.class, Forest.class})
class BloomingStingerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants another creature you control deathtouch until end of turn")
    void etbGrantsDeathtouchUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ChromeCompanion());
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Entering alone does not let Blooming Stinger target itself")
    void enteringAloneHasNoLegalTarget() {
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Blooming Stinger");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent you control")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Only the targeted creature gains deathtouch")
    void grantsDeathtouchOnlyToTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("ETB still grants deathtouch after Blooming Stinger leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        harness.setHand(player1, List.of(new BloomingStinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Blooming Stinger"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blooming Stinger");
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }
}
