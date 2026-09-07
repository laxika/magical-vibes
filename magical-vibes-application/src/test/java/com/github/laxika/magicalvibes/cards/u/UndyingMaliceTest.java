package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({UndyingMalice.class, DoomBlade.class, GrizzlyBears.class})
class UndyingMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the targeted creature tapped with a +1/+1 counter under its owner's control")
    void returnsTappedWithCounterUnderOwnersControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        var card = target.getCard();

        castUndyingMalice(target);
        destroyTarget(player1, target);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("The granted death ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        var card = target.getCard();

        castUndyingMalice(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroyTarget(player2, target);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    private void castUndyingMalice(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UndyingMalice()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyTarget(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
