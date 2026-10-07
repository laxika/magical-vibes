package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenStrike.class, GrizzlyBears.class})
class SuddenStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking creature")
    void destroysAttackingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a blocking creature")
    void destroysBlockingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setBlocking(true);

        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an idle creature")
    void rejectsIdleCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuddenStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new SuddenStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Can destroy a blocking creature you control")
    void destroysOwnBlockingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setBlocking(true);

        cast(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy a creature that leaves combat before resolution")
    void targetLeavingCombatBecomesIllegal() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);
        harness.setHand(player1, List.of(new SuddenStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sudden Strike");
    }

    @Test
    @DisplayName("Does not destroy a different creature when the target leaves the battlefield")
    void missingTargetDoesNotAffectOtherAttacker() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        other.setAttacking(true);
        harness.setHand(player1, List.of(new SuddenStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sudden Strike");
    }
}
