package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TempleBellTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Temple Bell has correct activated ability")
    void hasCorrectAbility() {
        TempleBell card = new TempleBell();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(EachPlayerDrawsCardEffect.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts draw effect on the stack")
    void activatingPutsOnStack() {
        addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Temple Bell");
    }

    @Test
    @DisplayName("Activating ability taps Temple Bell")
    void activatingTapsBell() {
        Permanent bell = addBellReady(player1);

        assertThat(bell.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(bell.isTapped()).isTrue();
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability causes each player to draw a card")
    void resolvingCausesEachPlayerToDraw() {
        addBellReady(player1);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(p2HandBefore + 1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate twice because it requires tap")
    void cannotActivateTwice() {
        addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Temple Bell stays on battlefield =====

    @Test
    @DisplayName("Temple Bell remains on battlefield after resolution")
    void remainsOnBattlefieldAfterResolution() {
        addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Temple Bell"));
    }

    // ===== Helper methods =====

    private Permanent addBellReady(Player player) {
        TempleBell card = new TempleBell();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
