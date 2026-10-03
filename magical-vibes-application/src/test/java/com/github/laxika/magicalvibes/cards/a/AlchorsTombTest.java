package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
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

    @Test
    void laterActivationReplacesThePreviouslyChosenColor() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        harness.addToBattlefield(player1, new AlchorsTomb());
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, apes.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 1, 0, null, apes.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, apes)).containsExactly(CardColor.WHITE);
        gd.expireEndOfTurnFloatingEffects();
        apes.resetModifiers();
        assertThat(gqs.getEffectiveColors(gd, apes)).containsExactly(CardColor.WHITE);
    }

    @Test
    void cannotTargetAPermanentSpellOnTheStack() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        BarbaryApes apes = new BarbaryApes();
        harness.castFromHand(player1, apes, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, apes.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetThatChangesControllersBeforeResolutionIsNotRecolored() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, apes.getId());

        gd.playerBattlefields.get(player1.getId()).remove(apes);
        gd.playerBattlefields.get(player2.getId()).add(apes);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveColors(gd, apes)).containsExactly(CardColor.GREEN);
    }
}
