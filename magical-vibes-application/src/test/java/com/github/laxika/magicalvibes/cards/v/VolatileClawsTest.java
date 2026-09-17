package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileClaws.class, GrizzlyBears.class})
class VolatileClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts only creatures you control and grants all creature types")
    void boostsOwnCreaturesAndGrantsAllCreatureTypes() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.ELF)).isTrue();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(opponent, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new VolatileClaws()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
