package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonEavesdropper.class})
class LoxodonEavesdropperTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it investigates")
    void entersAndInvestigates() {
        harness.setHand(player1, List.of(new LoxodonEavesdropper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Drawing the second card each turn gives it +1/+1 and vigilance until end of turn")
    void secondDrawBoostsAndGivesVigilance() {
        Permanent eavesdropper = harness.addToBattlefieldAndReturn(player1, new LoxodonEavesdropper());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LoxodonEavesdropper(), new LoxodonEavesdropper(),
                new LoxodonEavesdropper(), new LoxodonEavesdropper(), new LoxodonEavesdropper()));

        drawCard(player1);
        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eavesdropper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isFalse();

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isTrue();

        drawCard(player1);
        assertThat(gd.stack).hasSize(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eavesdropper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isFalse();
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    @Test
    @DisplayName("Opponent draws do not trigger the ability")
    void opponentDrawsDoNotTrigger() {
        Permanent eavesdropper = harness.addToBattlefieldAndReturn(player1, new LoxodonEavesdropper());
        harness.setLibrary(player2, List.of(new LoxodonEavesdropper(), new LoxodonEavesdropper()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The second draw triggers during an opponent's turn")
    void secondDrawOnOpponentsTurnTriggers() {
        Permanent eavesdropper = harness.addToBattlefieldAndReturn(player1, new LoxodonEavesdropper());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new LoxodonEavesdropper(), new LoxodonEavesdropper()));

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A draw before this creature enters counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new LoxodonEavesdropper(), new LoxodonEavesdropper()));
        drawCard(player1);
        Permanent eavesdropper = harness.addToBattlefieldAndReturn(player1, new LoxodonEavesdropper());

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed to draw the second card")
    void clueDrawTriggersBoost() {
        harness.setHand(player1, List.of(new LoxodonEavesdropper()));
        harness.setLibrary(player1, List.of(new LoxodonEavesdropper(), new LoxodonEavesdropper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent eavesdropper = findPermanent(player1, "Loxodon Eavesdropper");
        Permanent clue = findPermanent(player1, "Clue");
        drawCard(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gqs.getEffectivePower(gd, eavesdropper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, eavesdropper, Keyword.VIGILANCE)).isTrue();
    }
}
