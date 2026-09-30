package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SemblanceScanner.class, GrizzlyBears.class})
class SemblanceScannerTest extends BaseCardTest {

    @Test
    void scannerDealsCombatDamageAndConjuresItsDuplicate() {
        Permanent scanner = addReadyScanner();
        scanner.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Semblance Scanner");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void equippedCreatureDealsCombatDamageAndItsDuplicateIsConjured() {
        Permanent scanner = addReadyScanner();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        scanner.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Grizzly Bears");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void tokenCombatDamageSourceDoesNotConjureADuplicate() {
        SemblanceScanner scannerCard = new SemblanceScanner();
        scannerCard.setToken(true);
        Permanent scanner = addCreatureReady(player1, scannerCard);
        scanner.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyScanner() {
        return addCreatureReady(player1, new SemblanceScanner());
    }
}
