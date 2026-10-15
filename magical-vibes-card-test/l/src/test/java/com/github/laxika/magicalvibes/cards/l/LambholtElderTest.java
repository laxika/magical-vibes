package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GravetillerWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LambholtElder.class, GravetillerWurm.class})
class LambholtElderTest extends BaseCardTest {

    @Test
    @DisplayName("Lambholt Elder does not draw when dealing combat damage to a player")
    void frontFaceDoesNotDrawOnCombatDamage() {
        Permanent elder = addCreatureReady(player1, new LambholtElder());
        elder.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage is dealt to a player")
    void noDrawWhenBlocked() {
        Permanent elder = addCreatureReady(player1, new LambholtElder());
        elder.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GravetillerWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        harness.assertInGraveyard(player1, "Lambholt Elder");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Transforms to Silverpelt Werewolf when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new LambholtElder());

        gd.spellsCastLastTurn.clear();

        forceUpkeep(player2);
        harness.passBothPriorities();

        assertThat(elder.isTransformed()).isTrue();
        assertThat(elder.getCard().getName()).isEqualTo("Silverpelt Werewolf");
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new LambholtElder());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        forceUpkeep(player2);

        assertThat(elder.isTransformed()).isFalse();
        assertThat(elder.getCard().getName()).isEqualTo("Lambholt Elder");
    }

    @Test
    @DisplayName("Silverpelt Werewolf draws one card when dealing combat damage to a player")
    void backFaceDrawsOneCardOnCombatDamage() {
        Permanent elder = addCreatureReady(player1, new LambholtElder());
        transformToBackFace(elder);
        elder.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Silverpelt Werewolf transforms back when a player cast two or more spells last turn")
    void silverpeltTransformsBackWhenTwoSpellsCast() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new LambholtElder());
        transformToBackFace(elder);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        forceUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elder.isTransformed()).isFalse();
        assertThat(elder.getCard().getName()).isEqualTo("Lambholt Elder");
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Silverpelt Werewolf does not transform back when only one spell was cast last turn")
    void silverpeltDoesNotTransformBackWhenOnlyOneSpellCast() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new LambholtElder());
        transformToBackFace(elder);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        forceUpkeep(player1);

        assertThat(elder.isTransformed()).isTrue();
        assertThat(elder.getCard().getName()).isEqualTo("Silverpelt Werewolf");
    }

    @Test
    @DisplayName("Silverpelt Werewolf does not draw when its combat damage is dealt only to a blocker")
    void backFaceDoesNotDrawWhenBlocked() {
        Permanent elder = addCreatureReady(player1, new LambholtElder());
        transformToBackFace(elder);
        elder.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GravetillerWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Gravetiller Wurm");
        harness.assertOnBattlefield(player1, "Silverpelt Werewolf");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Silverpelt Werewolf stays transformed when no spells were cast last turn")
    void backFaceStaysTransformedWhenNoSpellsCast() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new LambholtElder());
        transformToBackFace(elder);
        gd.spellsCastLastTurn.clear();

        forceUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elder.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Silverpelt Werewolf");
    }

    private void forceUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void transformToBackFace(Permanent elder) {
        gd.spellsCastLastTurn.clear();
        forceUpkeep(player2);
        harness.passBothPriorities();
        elder.untap();
        elder.setSummoningSick(false);
        assertThat(elder.isTransformed()).isTrue();
    }
}
