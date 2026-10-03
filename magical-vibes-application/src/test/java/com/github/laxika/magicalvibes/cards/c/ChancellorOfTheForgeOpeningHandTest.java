package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CardUsedExtension.class)
@CardUsed({ChancellorOfTheForge.class})
class ChancellorOfTheForgeOpeningHandTest {

    @Test
    void revealIsPregameAndAcceptedRevealCreatesMandatoryUpkeepToken() {
        GameTestHarness harness = new GameTestHarness();
        var player = harness.getPlayer1();
        var gd = harness.getGameData();
        ChancellorOfTheForge chancellor = new ChancellorOfTheForge();
        harness.setHand(player, List.of(chancellor));
        harness.setHand(harness.getPlayer2(), List.of());

        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player.getId())).isEmpty();
        harness.handleMayAbilityChosen(player, true);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player.getId())).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(gd.playerHands.get(player.getId())).contains(chancellor);
    }

    @Test
    void decliningPregameRevealCreatesNoUpkeepTrigger() {
        GameTestHarness harness = new GameTestHarness();
        var player = harness.getPlayer1();
        var gd = harness.getGameData();
        harness.setHand(player, List.of(new ChancellorOfTheForge()));
        harness.setHand(harness.getPlayer2(), List.of());

        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player.getId())).isEmpty();
    }
}
