package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.cards.v.VisionSkeins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusGuildmage.class, AzoriusSignet.class, SimicRagworm.class, VisionSkeins.class})
class AzoriusGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps target creature")
    void tapsTargetCreature() {
        addGuildmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        addManaForWhiteAbility(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addGuildmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        addManaForWhiteAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability counters an activated ability")
    void countersActivatedAbility() {
        addGuildmage(player1);
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        ragworm.setSummoningSick(false);
        ragworm.tap();
        harness.addMana(player2, ManaColor.BLUE, 1);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, ragworm.getCard().getId());
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability cannot target a spell")
    void cannotTargetSpell() {
        addGuildmage(player1);
        VisionSkeins visionSkeins = new VisionSkeins();
        harness.setHand(player2, List.of(visionSkeins));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, visionSkeins.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a mana ability")
    void cannotTargetManaAbility() {
        addGuildmage(player1);
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        signet.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, signet.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addGuildmage(Player player) {
        harness.addToBattlefield(player, new AzoriusGuildmage());
    }

    private void addManaForWhiteAbility(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private void addManaForBlueAbility(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
