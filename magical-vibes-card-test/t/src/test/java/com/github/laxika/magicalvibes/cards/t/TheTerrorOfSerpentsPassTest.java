package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.z.ZukosOffense;
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

@CardUsed({TheTerrorOfSerpentsPass.class, ZukosOffense.class})
class TheTerrorOfSerpentsPassTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can target The Terror of Serpent's Pass with spells")
    void controllerCanTargetWithSpells() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new TheTerrorOfSerpentsPass());
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, terror.getId());

        assertThat(terror.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(terror);
        harness.assertInGraveyard(player1, "Zuko's Offense");
    }

    @Test
    @DisplayName("Opponent cannot target The Terror of Serpent's Pass with spells")
    void opponentCannotTargetWithSpells() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new TheTerrorOfSerpentsPass());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new ZukosOffense()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, terror.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

}
