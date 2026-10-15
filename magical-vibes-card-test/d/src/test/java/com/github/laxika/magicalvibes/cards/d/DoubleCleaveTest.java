package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.h.HoofSkulkin;
import com.github.laxika.magicalvibes.cards.s.SpringjackPasture;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoubleCleave.class, HoofSkulkin.class, SpringjackPasture.class})
class DoubleCleaveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Double Cleave grants double strike to target creature")
    void resolvingGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Hoof Skulkin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Hoof Skulkin");
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature with double strike deals damage to player in both combat phases")
    void doubleStrikeDealsDoublePlayerDamage() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new HoofSkulkin());
        attacker.setAttacking(true);
        attacker.getGrantedKeywords().add(Keyword.DOUBLE_STRIKE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.passBothPriorities();

        // Hoof Skulkin (2/2) with double strike deals 2 + 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Double strike keyword is removed at end of turn")
    void doubleStrikeRemovedAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        bears.getGrantedKeywords().add(Keyword.DOUBLE_STRIKE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double Cleave fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Hoof Skulkin");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Double Cleave");
    }

    @Test
    @DisplayName("Double Cleave can target an opponent's creature and be paid with white mana")
    void targetsOpposingCreatureWithWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HoofSkulkin());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(other.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Double Cleave");
    }

    @Test
    @DisplayName("Double Cleave cannot target a noncreature land")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SpringjackPasture());
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving Double Cleave causes damage in both combat damage steps")
    void resolvedSpellDealsDoubleCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new HoofSkulkin());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Double Cleave's resolved grant expires at cleanup")
    void resolvedGrantExpiresAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
