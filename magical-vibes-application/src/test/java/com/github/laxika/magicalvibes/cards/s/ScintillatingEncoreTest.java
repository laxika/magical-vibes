package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScintillatingEncore.class, DoomBlade.class, GrizzlyBears.class})
class ScintillatingEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually gives a creature you control +2/+0")
    void perpetuallyBoostsTargetCreature() {
        Permanent target = addCreature(player1);

        castEncore(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns the creature tapped under its owner's control when it dies this turn")
    void returnsTargetTappedWhenItDiesThisTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        harness.setHand(player1, List.of(new ScintillatingEncore(), new DoomBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new ScintillatingEncore()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castEncore(UUID targetId) {
        harness.setHand(player1, List.of(new ScintillatingEncore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
