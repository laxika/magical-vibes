package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchorsTomb.class, BarbaryApes.class})
class AlchorsTombTest extends BaseCardTest {

    @Test
    void targetBecomesOneChosenColorIndefinitely() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AlchorsTomb());
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, apes.getId());
        assertThat(tomb.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, apes)).containsExactly(CardColor.RED);

        gd.expireEndOfTurnFloatingEffects();
        apes.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, apes)).containsExactly(CardColor.RED);
    }

    @Test
    void canTargetOnlyAPermanentYouControl() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        Permanent opponentApes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentApes.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetANoncreaturePermanentYouControl() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AlchorsTomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, tomb.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, tomb)).containsExactly(CardColor.BLUE);
    }
}
