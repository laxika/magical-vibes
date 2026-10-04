package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FlipTheSwitch;
import com.github.laxika.magicalvibes.cards.w.Waildrifter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Galedrifter.class, Waildrifter.class, FlipTheSwitch.class})
class GaledrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Normally cast Galedrifter enters front face up and goes to the graveyard when removed")
    void normalCastDoesNotApplyBackFaceExileReplacement() {
        Galedrifter card = new Galedrifter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.isTransformed()).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Disturb cannot be paid with only the front face's mana cost")
    void disturbRequiresFiveMana() {
        Galedrifter card = new Galedrifter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A countered Waildrifter spell is exiled instead of returning to the graveyard")
    void counteredDisturbSpellIsExiled() {
        Galedrifter card = new Galedrifter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new FlipTheSwitch()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, card.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(card.getId());
    }

    @Test
    @DisplayName("Disturb casts Galedrifter from the graveyard transformed as Waildrifter")
    void disturbEntersTransformed() {
        Permanent waildrifter = castWithDisturb();

        assertThat(waildrifter.isTransformed()).isTrue();
        assertThat(waildrifter.getCard().getName()).isEqualTo("Waildrifter");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Waildrifter is exiled instead of going to the graveyard")
    void waildrifterIsExiledInsteadOfGraveyard() {
        Permanent waildrifter = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, waildrifter));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(waildrifter.getOriginalCard().getId());
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Galedrifter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, null);
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
