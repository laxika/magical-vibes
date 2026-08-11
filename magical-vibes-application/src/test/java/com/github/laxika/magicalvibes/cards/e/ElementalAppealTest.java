package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Elemental Appeal")
class ElementalAppealTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 7/1 red Elemental token with trample and haste")
    void createsElementalToken() {
        cast(false);

        Permanent token = elementalToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The kicked spell gives its token +7/+0 until end of turn")
    void kickedTokenGetsBoost() {
        cast(true);

        Permanent token = elementalToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("The kicked token boost expires at cleanup")
    void kickedTokenBoostExpiresAtCleanup() {
        cast(true);
        Permanent token = elementalToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(14);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
    }

    @Test
    @DisplayName("The token is exiled at the beginning of the next end step")
    void tokenIsExiledAtNextEndStep() {
        cast(false);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    private void cast(boolean kicked) {
        harness.setHand(player1, List.of(new ElementalAppeal()));
        harness.addMana(player1, ManaColor.RED, kicked ? 9 : 4);
        if (kicked) {
            harness.castKickedSorcery(player1, 0);
        } else {
            harness.castSorcery(player1, 0, 0);
        }
        harness.passBothPriorities();
    }

    private Permanent elementalToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
    }
}
