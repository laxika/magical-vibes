package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FireAmbush;
import com.github.laxika.magicalvibes.cards.f.FireBowman;
import com.github.laxika.magicalvibes.cards.w.WieldingTheGreenDragon;
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

@CardUsed({TaoistHermit.class, FireAmbush.class, WieldingTheGreenDragon.class, FireBowman.class})
class TaoistHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Taoist Hermit with spells")
    void opponentCannotTarget() {
        Permanent hermit = addCreatureReady(player1, new TaoistHermit());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FireAmbush()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, hermit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Taoist Hermit with spells")
    void controllerCanTarget() {
        Permanent hermit = addCreatureReady(player1, new TaoistHermit());

        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, hermit.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(hermit.getId());
    }

    @Test
    @DisplayName("Opponent cannot target Taoist Hermit with abilities")
    void opponentCannotTargetWithAbility() {
        Permanent hermit = addCreatureReady(player1, new TaoistHermit());
        Permanent fireBowman = addCreatureReady(player2, new FireBowman());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int fireBowmanIndex = gd.playerBattlefields.get(player2.getId()).indexOf(fireBowman);

        assertThatThrownBy(() -> harness.activateAbility(player2, fireBowmanIndex, null, hermit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
