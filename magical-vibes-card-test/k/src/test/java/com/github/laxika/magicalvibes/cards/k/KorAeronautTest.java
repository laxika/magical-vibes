package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorAeronaut.class, StoneworkPuma.class})
class KorAeronautTest extends BaseCardTest {

    @Nested
    @DisplayName("Cast without kicker")
    @CardUsed({KorAeronaut.class, StoneworkPuma.class})
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
    @CardUsed({KorAeronaut.class, StoneworkPuma.class})
    class WithKicker {

        @Test
        @DisplayName("Can be kicked on an empty battlefield and target itself on entry")
        void canTargetItselfAfterEntering() {
            harness.setHand(player1, List.of(new KorAeronaut()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castKickedCreature(player1, 0);
            harness.passBothPriorities();

            Permanent aeronaut = gd.playerBattlefields.get(player1.getId()).getFirst();
            harness.handlePermanentChosen(player1, aeronaut.getId());
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(aeronaut.getId());

            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Kor Aeronaut");
            assertThat(gd.stack).isEmpty();
        }

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

        @Test
        @DisplayName("Can grant flying to a creature you control")
        void grantsFlyingToOwnCreature() {
            Permanent target = addCreature(player1);
            castKicked(target.getId());

            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Trigger still resolves after Kor Aeronaut leaves the battlefield")
        void triggerSurvivesSourceLeaving() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
            gd.playerBattlefields.get(player1.getId()).remove(source);
            gd.playerGraveyards.get(player1.getId()).add(source.getCard());

            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not grant flying when its target leaves before resolution")
        void targetLeavingPreventsGrant() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            gd.playerBattlefields.get(player2.getId()).remove(target);
            gd.playerGraveyards.get(player2.getId()).add(target.getCard());

            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new StoneworkPuma());
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
