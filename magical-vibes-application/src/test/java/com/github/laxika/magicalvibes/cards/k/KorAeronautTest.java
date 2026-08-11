package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KorAeronautTest extends BaseCardTest {

    @Nested
    @DisplayName("Cast without kicker")
    class WithoutKicker {

        @Test
        @DisplayName("Enters without an ETB trigger")
        void entersWithoutTrigger() {
            addCreature(player2);
            harness.setHand(player1, List.of(new KorAeronaut()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Kor Aeronaut");
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cast with kicker")
    class WithKicker {

        @Test
        @DisplayName("ETB trigger grants target creature flying until end of turn")
        void grantsFlyingUntilEndOfTurn() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        }
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castKicked(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KorAeronaut()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
