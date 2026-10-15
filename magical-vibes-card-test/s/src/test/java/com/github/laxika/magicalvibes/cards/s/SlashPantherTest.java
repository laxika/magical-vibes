package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlashPanther.class})
class SlashPantherTest extends BaseCardTest {

    @Test
    void castsWithRedManaWithoutPayingLife() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slash Panther");
        harness.assertLife(player1, 20);
    }

    @Test
    void castsWithGenericManaAndTwoLifeAndCanAttackImmediately() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        Permanent panther = findPermanent(player1, "Slash Panther");
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(panther.isAttacking()).isTrue();
        assertThat(panther.isTapped()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 16);
    }

    @Test
    void canSpendRedManaOnGenericCostAndPayLifeForPhyrexianSymbol() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slash Panther");
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotPayPhyrexianSymbolWithLessThanTwoLife() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Slash Panther");
        harness.assertLife(player1, 1);
    }

    @Test
    void canPayRedManaInsteadOfLifeAtOneLife() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slash Panther");
        harness.assertLife(player1, 1);
    }

    @Test
    void payingLifeDoesNotReplaceGenericManaRequirement() {
        harness.setHand(player1, List.of(new SlashPanther()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Slash Panther");
        harness.assertLife(player1, 20);
    }
}
