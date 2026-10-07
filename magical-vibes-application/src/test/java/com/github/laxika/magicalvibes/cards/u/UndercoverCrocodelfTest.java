package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercoverCrocodelf.class})
class UndercoverCrocodelfTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when it deals combat damage to a player")
    void investigatesOnCombatDamageToPlayer() {
        Permanent crocodelf = addCreatureReady(player1, new UndercoverCrocodelf());
        crocodelf.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when it deals no combat damage to a player")
    void doesNotInvestigateWithoutCombatDamageToPlayer() {
        addCreatureReady(player1, new UndercoverCrocodelf());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Disguise casts it face down and turns it face up")
    void disguiseCastsAndTurnsFaceUp() {
        UndercoverCrocodelf card = new UndercoverCrocodelf();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crocodelf = findPermanentForCard(card);
        assertThat(crocodelf.isFaceDown()).isTrue();
        assertThat(gqs.hasKeyword(gd, crocodelf, Keyword.WARD)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crocodelf));

        assertThat(crocodelf.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Face-down combat damage does not investigate")
    void faceDownCombatDamageDoesNotInvestigate() {
        Permanent crocodelf = castFaceDownCrocodelf();
        crocodelf.setSummoningSick(false);
        crocodelf.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Disguise cost can be paid with two blue mana")
    void turnsFaceUpWithBlueMana() {
        Permanent crocodelf = castFaceDownCrocodelf();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crocodelf));

        assertThat(crocodelf.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, crocodelf, Keyword.WARD)).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not investigate")
    void combatDamageToCreatureDoesNotInvestigate() {
        addCreatureReady(player1, new UndercoverCrocodelf());
        addCreatureReady(player2, new UndercoverCrocodelf());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Undercover Crocodelf");
        harness.assertInGraveyard(player2, "Undercover Crocodelf");
    }

    @Test
    @DisplayName("Turning face up with mixed hybrid mana restores the investigate ability")
    void turnsFaceUpWithMixedManaAndInvestigates() {
        Permanent crocodelf = castFaceDownCrocodelf();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crocodelf));
        crocodelf.setSummoningSick(false);
        crocodelf.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        Permanent crocodelf = addCreatureReady(player1, new UndercoverCrocodelf());
        crocodelf.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        UndercoverCrocodelf draw = new UndercoverCrocodelf();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    private Permanent castFaceDownCrocodelf() {
        UndercoverCrocodelf card = new UndercoverCrocodelf();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanentForCard(card);
    }

    private Permanent findPermanentForCard(UndercoverCrocodelf card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
