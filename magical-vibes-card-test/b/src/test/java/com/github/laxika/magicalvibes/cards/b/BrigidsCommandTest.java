package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ClachanFestival;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrigidsCommand.class, GoldmeadowStalwart.class, GrizzlyBears.class, HillGiant.class, ClachanFestival.class})
class BrigidsCommandTest extends BaseCardTest {

    @Test
    void copyKithkinAndCreateTokenForTargetPlayer() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(kithkin.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .extracting(permanent -> permanent.getCard().isToken())
                .isEqualTo(true);
    }

    @Test
    void boostAndFightModesResolveInCardTextOrder() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(creature.getId(), creature.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void copyModeRejectsKithkinControlledByOpponent() {
        Permanent opponentKithkin = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(opponentKithkin.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyAndBoostCanShareTargetWithoutBoostingTheCopy() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(kithkin.getId(), kithkin.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> {
                    assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
                });
    }

    @Test
    void copyIsCreatedBeforeOriginalDiesInFight() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(kithkin.getId(), kithkin.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goldmeadow Stalwart");
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(copy -> {
                    assertThat(copy.getCard().isToken()).isTrue();
                    assertThat(copy.getMarkedDamage()).isZero();
                    assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
                });
    }

    @Test
    void casterCanCreateTokenAndBoostAnExistingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void tokenCreationStillResolvesWhenCopyTargetLeavesBattlefield() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(kithkin.getId(), player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(kithkin);
        gd.playerGraveyards.get(player1.getId()).add(kithkin.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    void tokenCreationAndFightUseTheirOwnTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(player1.getId(), creature.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getMarkedDamage()).isZero();
                });
    }

    @Test
    void copyModeAcceptsNoncreatureKithkinAndCopiesItsEnterAbility() {
        Permanent festival = harness.addToBattlefieldAndReturn(player1, new ClachanFestival());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(festival.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }
}
