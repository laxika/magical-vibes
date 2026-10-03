package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssureAssemble.class, VernadiShieldmate.class, SelesnyaGuildgate.class})
class AssureAssembleTest extends BaseCardTest {

    private static final int ASSURE = 0;
    private static final int ASSEMBLE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Assure puts a counter on a creature and grants indestructible")
    void assureCountersAndProtectsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalInstant(player1, 0, ASSURE, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Assemble creates three vigilant Elf Knight tokens")
    void assembleCreatesElfKnightTokens() {
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, ASSEMBLE, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.KNIGHT);
                    assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Both halves cannot be cast together because the card has no fuse")
    void cannotFuseBothHalves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Assure's indestructible grant wears off at end of turn")
    void indestructibleWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalInstant(player1, 0, ASSURE, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Assure cannot target a noncreature permanent")
    void assureCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SelesnyaGuildgate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, ASSURE, List.of(harness.getPermanentId(player1, "Selesnya Guildgate"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assureCanTargetOpponentsCreatureUsingWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstant(player1, 0, ASSURE, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void assureDoesNotResolveWhenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new AssureAssemble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalInstant(player1, 0, ASSURE, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Assure // Assemble");
    }
}
