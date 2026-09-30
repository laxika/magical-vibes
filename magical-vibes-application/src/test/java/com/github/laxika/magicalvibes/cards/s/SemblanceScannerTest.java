package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SemblanceScanner.class, GrizzlyBears.class})
class SemblanceScannerTest extends BaseCardTest {

    @Test
    void scannerDealsCombatDamageAndConjuresItsDuplicate() {
        harness.setHand(player1, List.of());
        Permanent scanner = addReadyScanner();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scanner)));

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
        harness.setHand(player1, List.of());
        Permanent scanner = addReadyScanner();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        scanner.setAttachedTo(creature.getId());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

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
        harness.setHand(player1, List.of());
        SemblanceScanner scannerCard = new SemblanceScanner();
        scannerCard.setToken(true);
        Permanent scanner = addCreatureReady(player1, scannerCard);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scanner)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyScanner() {
        return addCreatureReady(player1, new SemblanceScanner());
    }
}
