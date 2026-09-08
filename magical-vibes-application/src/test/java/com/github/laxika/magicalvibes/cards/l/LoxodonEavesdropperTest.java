package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({LoxodonEavesdropper.class, GrizzlyBears.class})
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
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

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

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
