package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({Galedrifter.class, Waildrifter.class})
class GaledrifterTest extends BaseCardTest {

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

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
