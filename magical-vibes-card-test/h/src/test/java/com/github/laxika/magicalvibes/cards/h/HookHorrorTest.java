package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMannequin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HookHorror.class, Murder.class, MakeshiftMannequin.class})
class HookHorrorTest extends BaseCardTest {

    @Test
    void perpetuallyShrinksAndReturnsUntilItsToughnessIsZero() {
        Permanent hookHorror = harness.addToBattlefieldAndReturn(player1, new HookHorror());

        destroyWithMurder(player2, hookHorror);
        hookHorror = findPermanents(player1, "Hook Horror").getFirst();
        assertThat(gqs.getEffectivePower(gd, hookHorror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hookHorror)).isEqualTo(2);

        destroyWithMurder(player2, hookHorror);
        hookHorror = findPermanents(player1, "Hook Horror").getFirst();
        assertThat(gqs.getEffectivePower(gd, hookHorror)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hookHorror)).isEqualTo(1);

        destroyWithMurder(player2, hookHorror);

        harness.assertNotOnBattlefield(player1, "Hook Horror");
        harness.assertInGraveyard(player1, "Hook Horror");
    }

    @Test
    void returnsToItsOwnersBattlefieldWhenControlledByAnotherPlayer() {
        HookHorror card = new HookHorror();
        card.setOwnerId(player1.getId());
        Permanent hookHorror = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(hookHorror.getId(), player1.getId());

        destroyWithMurder(player2, hookHorror);

        harness.assertNotOnBattlefield(player2, "Hook Horror");
        hookHorror = findPermanents(player1, "Hook Horror").getFirst();
        assertThat(gqs.getEffectivePower(gd, hookHorror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hookHorror)).isEqualTo(2);
    }

    @Test
    void battlefieldCountersDoNotChangeTheGraveyardToughnessCheck() {
        Permanent hookHorror = harness.addToBattlefieldAndReturn(player1, new HookHorror());
        hookHorror.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        destroyWithMurder(player2, hookHorror);

        Permanent returned = findPermanents(player1, "Hook Horror").getFirst();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void deathTriggerDoesNotModifyANewBattlefieldObject() {
        Permanent hookHorror = harness.addToBattlefieldAndReturn(player1, new HookHorror());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, hookHorror.getId());
        harness.assertInGraveyard(player1, "Hook Horror");

        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, hookHorror.getCard().getId());
        Permanent returned = findPermanents(player1, "Hook Horror").getFirst();
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    private void destroyWithMurder(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
