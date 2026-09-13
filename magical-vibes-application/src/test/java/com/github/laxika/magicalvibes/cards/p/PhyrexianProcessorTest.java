package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PhyrexianProcessor.class)
class PhyrexianProcessorTest extends BaseCardTest {

    @Test
    @DisplayName("Stores the life paid on entry and creates a token of that size")
    void createsTokenSizedByLifePaidOnEntry() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.PayAnyAmountOfLifeAsEnters.class);

        harness.handleListChoice(player1, "5");

        Permanent processor = findPermanent(player1, "Phyrexian Processor");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int processorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(processor);
        harness.activateAbility(player1, processorIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Phyrexian Minion");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(processor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying 0 life creates a 0/0 token that dies immediately")
    void payingZeroLifeCreatesTokenThatDiesImmediately() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "0");

        Permanent processor = findPermanent(player1, "Phyrexian Processor");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int processorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(processor);
        harness.activateAbility(player1, processorIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Phyrexian Minion"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(processor.isTapped()).isTrue();
    }
}
