package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirbendersReversal.class, GrizzlyBears.class})
class AirbendersReversalTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys an attacking creature")
    void destroyModeDestroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        cast(0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The airbend mode exiles a creature you control")
    void airbendModeExilesOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, creature.getId());

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(creature.getOriginalCard().getId()))
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Each mode enforces its own target restriction")
    void modesRejectIllegalTargets() {
        Permanent nonAttackingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{0}, List.of(nonAttackingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{1}, List.of(nonAttackingCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{mode}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
