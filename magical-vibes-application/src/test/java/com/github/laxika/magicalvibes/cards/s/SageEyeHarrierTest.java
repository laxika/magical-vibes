package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SageEyeHarrier.class})
class SageEyeHarrierTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SageEyeHarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent harrier = findPermanent(player1, "Sage-Eye Harrier");
        assertThat(harrier.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int harrierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harrier);
        harness.turnFaceUp(player1, harrierIndex);
        harness.passBothPriorities();

        assertThat(harrier.isFaceDown()).isFalse();
    }

    @Test
    void morphRequiresWhiteManaAndTurnsFaceUpWithoutUsingTheStack() {
        harness.setHand(player1, List.of(new SageEyeHarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent harrier = findPermanent(player1, "Sage-Eye Harrier");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(harrier.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(harrier.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(harrier);
    }

    @Test
    void faceDownHarrierCannotBlockFlyingUntilTurnedFaceUp() {
        harness.setHand(player1, List.of(new SageEyeHarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent blocker = findPermanent(player1, "Sage-Eye Harrier");
        Permanent attacker = addCreatureReady(player2, new SageEyeHarrier());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    void faceDownHarrierDealsTwoCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SageEyeHarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent harrier = findPermanent(player1, "Sage-Eye Harrier");
        harrier.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(harrier.isFaceDown()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
