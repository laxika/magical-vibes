package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrownInFilthTest extends BaseCardTest {

    private void castWithLibrary(List<Card> library, UUID targetId) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DrownInFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Four milled lands give -4/-4 and kill a 2/2")
    void millsFourLandsAndKillsBear() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        castWithLibrary(List.of(new Forest(), new Forest(), new Forest(), new Forest()), bearId);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only lands among the milled cards count, and the debuff wears off at cleanup")
    void countsOnlyLandsAndWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        castWithLibrary(
                List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()), bearId);

        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DrownInFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }
}
