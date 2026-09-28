package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HookHorror.class, Murder.class})
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

    private void destroyWithMurder(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
