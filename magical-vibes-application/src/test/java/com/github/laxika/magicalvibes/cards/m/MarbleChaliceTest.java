package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarbleChalice.class})
class MarbleChaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Marble Chalice and puts it on the stack")
    void activatingTapsAndStacks() {
        Permanent chalice = harness.addToBattlefieldAndReturn(player1, new MarbleChalice());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(chalice.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolving ability gains 1 life for controller only")
    void resolvingGainsOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MarbleChalice());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate again while Marble Chalice is tapped")
    void cannotActivateTwice() {
        harness.addToBattlefield(player1, new MarbleChalice());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability resolves even if Marble Chalice leaves the battlefield")
    void abilityResolvesWithoutSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MarbleChalice());
        harness.activateAbility(player1, 0, null, null);

        Permanent chalice = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(chalice.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping Marble Chalice permits another activation in the same turn")
    void canActivateAgainAfterUntapping() {
        harness.setLife(player1, 20);
        Permanent chalice = harness.addToBattlefieldAndReturn(player1, new MarbleChalice());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        chalice.setTapped(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(chalice.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
