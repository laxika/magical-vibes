package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void tokenScannerDoesNotTriggerAtAll() {
        SemblanceScanner token = new SemblanceScanner();
        token.setToken(true);
        Permanent scanner = addCreatureReady(player1, token);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scanner)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gameLogContains("Semblance Scanner's combat damage trigger goes on the stack.")).isFalse();
    }

    @Test
    void equippedTokenDoesNotTriggerAtAll() {
        Permanent scanner = addReadyScanner();
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent creature = addCreatureReady(player1, token);
        scanner.setAttachedTo(creature.getId());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gameLogContains("Semblance Scanner's combat damage trigger goes on the stack.")).isFalse();
    }

    @Test
    void reconfigureAttachesMovesAndUnattaches() {
        Permanent scanner = addReadyScanner();
        Permanent first = addReadyScanner();
        Permanent second = addReadyScanner();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(scanner.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.isCreature(gd, scanner)).isFalse();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(scanner.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, scanner)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(scanner.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, scanner)).isTrue();
    }

    @Test
    void reconfigureRejectsOpponentCreature() {
        Permanent scanner = addReadyScanner();
        Permanent opponent = addCreatureReady(player2, new SemblanceScanner());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scanner.getAttachedTo()).isNull();
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent scanner = addReadyScanner();
        Permanent creature = addReadyScanner();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        scanner.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scanner.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void unattachRequiresAnAttachedScanner() {
        addReadyScanner();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reconfigureWorksWhileTappedAndSummoningSick() {
        Permanent scanner = harness.addToBattlefieldAndReturn(player1, new SemblanceScanner());
        scanner.tap();
        scanner.setSummoningSick(true);
        Permanent creature = addReadyScanner();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scanner.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(scanner.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, scanner)).isFalse();
    }

    private Permanent addReadyScanner() {
        return addCreatureReady(player1, new SemblanceScanner());
    }
}
