package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Stasis;
import com.github.laxika.magicalvibes.cards.v.Vindicate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisplayOfDominance.class, GrizzlyBears.class, Shock.class, Stasis.class, Vindicate.class})
class DisplayOfDominanceTest extends BaseCardTest {

    @Test
    void destroyModeDestroysBlueNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stasis());

        castDisplayOfDominance(0, target.getId());

        harness.assertNotOnBattlefield(player2, "Stasis");
        harness.assertInGraveyard(player2, "Stasis");
    }

    @Test
    void destroyModeCannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DisplayOfDominance()));
        addDisplayMana(player1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue or black noncreature permanent");
    }

    @Test
    void protectionModeBlocksOpponentBlackSpells() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Stasis());

        castDisplayOfDominance(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Vindicate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target");
    }

    @Test
    void protectionModeAllowsOwnBlackSpells() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Stasis());

        castDisplayOfDominance(1);

        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stasis");
        harness.assertInGraveyard(player1, "Stasis");
    }

    @Test
    void protectionModeAllowsOpponentRedSpells() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDisplayOfDominance(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private void castDisplayOfDominance(int mode, UUID... targetIds) {
        harness.setHand(player1, List.of(new DisplayOfDominance()));
        addDisplayMana(player1);
        harness.castModalInstant(player1, 0, mode, List.of(targetIds));
        harness.passBothPriorities();
    }

    private void addDisplayMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
