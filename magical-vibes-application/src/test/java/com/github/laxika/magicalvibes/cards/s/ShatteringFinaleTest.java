package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteringFinale.class, ShivanHellkite.class, GrizzlyBears.class})
class ShatteringFinaleTest extends BaseCardTest {

    @Test
    void perpetuallyGivesTargetOpponentCreatureMinusZeroMinusThree() {
        Permanent target = addCreatureReady(player2, new ShivanHellkite());

        castShatteringFinale(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void casualtyCopiesTheSpell() {
        Permanent target = addCreatureReady(player2, new ShivanHellkite());
        Permanent casualty = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatteringFinale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shivan Hellkite");
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new ShivanHellkite());
        harness.setHand(player1, List.of(new ShatteringFinale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private void castShatteringFinale(Permanent target) {
        harness.setHand(player1, List.of(new ShatteringFinale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
