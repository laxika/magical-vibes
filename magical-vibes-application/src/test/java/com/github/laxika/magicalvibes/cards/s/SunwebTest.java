package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunwebTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Sunweb onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Sunweb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sunweb"));
    }

    @Test
    @DisplayName("Sunweb can block a creature with power 3 or greater")
    void canBlockHighPowerCreature() {
        Permanent sunweb = new Permanent(new Sunweb());
        sunweb.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sunweb);

        Permanent atkPerm = new Permanent(new HillGiant()); // 3/3
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sunweb.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sunweb cannot block a creature with power 2 or less")
    void cannotBlockLowPowerCreature() {
        Permanent sunweb = new Permanent(new Sunweb());
        sunweb.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sunweb);

        Permanent atkPerm = new Permanent(new GrizzlyBears()); // 2/2
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 3 or greater");
    }
}
