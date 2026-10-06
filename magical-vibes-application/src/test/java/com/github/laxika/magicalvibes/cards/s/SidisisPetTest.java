package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidisisPet.class})
class SidisisPetTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlack() {
        harness.setHand(player1, List.of(new SidisisPet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pet = findPermanent(player1, "Sidisi's Pet");
        assertThat(pet.isFaceDown()).isTrue();
        assertThat(pet.getEffectivePower()).isEqualTo(2);
        assertThat(pet.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pet));
        assertThat(pet.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(pet.isFaceDown()).isFalse();
        assertThat(pet.getEffectivePower()).isEqualTo(1);
        assertThat(pet.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void faceUpPetGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SidisisPet());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void faceDownPetDealsCombatDamageWithoutGainingLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SidisisPet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent pet = findPermanent(player1, "Sidisi's Pet");
        pet.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(pet.isFaceDown()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void turnedFaceUpPetGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SidisisPet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent pet = findPermanent(player1, "Sidisi's Pet");
        pet.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, 0);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(pet.isFaceDown()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
