package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Glamermite.class, Forest.class})
class GlamermiteTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing tap taps the target creature")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Glamermite());

        castGlamermite(0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing untap untaps the target creature")
    void untapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Glamermite());
        target.tap();

        castGlamermite(1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Glamermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tap target creature");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Chooses the mode and target after entering and can tap itself")
    void canChooseItselfAfterEnteringAnEmptyBattlefield() {
        harness.setHand(player1, List.of(new Glamermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glamermite");
        UUID sourceId = harness.getPermanentId(player1, "Glamermite");
        harness.handleListChoice(player1, "Tap target creature");
        harness.handlePermanentChosen(player1, sourceId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast allows choosing the untap mode")
    void canChooseUntapWhenEnteringWithoutBeingCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Glamermite());
        target.tap();

        harness.enterBattlefieldAndReturn(player1, new Glamermite());
        harness.handleListChoice(player1, "Untap target creature");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    private void castGlamermite(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new Glamermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0 ? "Tap target creature" : "Untap target creature");
        harness.handlePermanentChosen(player1, targetId);
    }
}
