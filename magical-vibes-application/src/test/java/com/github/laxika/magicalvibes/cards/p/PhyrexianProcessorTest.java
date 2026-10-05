package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianProcessor.class, Disenchant.class, PlatinumEmperion.class})
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

        harness.assertNotOnBattlefield(player1, "Phyrexian Minion");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(processor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The token ability remembers the payment after Processor is destroyed")
    void createsTokenAfterSourceIsDestroyed() {
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "7");

        Permanent processor = findPermanent(player1, "Phyrexian Processor");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passPriority(player1);
        harness.setHand(player2, java.util.List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, processor.getId());
        harness.assertInGraveyard(player1, "Phyrexian Processor");
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Phyrexian Minion");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(7);
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Repeated activations use the original payment without paying life again")
    void repeatedActivationsUseOriginalPayment() {
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "5");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Minion")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        });
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("A life total that cannot change permits only a zero life payment")
    void cannotPayPositiveLifeWithPlatinumEmperion() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "5"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "0");
        harness.assertLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Phyrexian Minion");
    }
}
