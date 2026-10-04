package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromeProwler.class})
class ChromeProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature an opponent controls")
    void selfEntryTapsOpponentCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        castProwler(player1, victim.getId());
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ChromeProwler());
        harness.setHand(player1, List.of(new ChromeProwler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, own.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castProwler(player1, victim.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Chrome Prowler");
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can enter when no opponent controls a creature")
    void entersWithoutLegalTarget() {
        castProwler(player1, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Chrome Prowler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An already tapped opposing creature is a legal target")
    void canTargetTappedCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        victim.tap();

        castProwler(player1, victim.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Chrome Prowler");
        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The creature enters before its tap trigger resolves")
    void tapTriggerResolvesSeparatelyFromCreatureSpell() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        castProwler(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chrome Prowler");
        assertThat(victim.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(victim.isTapped()).isTrue();
    }

    private void castProwler(Player player, UUID targetId) {
        harness.setHand(player, List.of(new ChromeProwler()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, 0, targetId);
    }
}
