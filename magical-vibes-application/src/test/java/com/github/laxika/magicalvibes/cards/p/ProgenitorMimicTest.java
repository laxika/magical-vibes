package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProgenitorMimic.class, KraulWarrior.class, BeetleformMage.class, Putrefy.class})
class ProgenitorMimicTest extends BaseCardTest {

    private Permanent copyKraulWarrior(boolean copy) {
        harness.addToBattlefield(player2, new KraulWarrior());
        harness.castFromHand(player1, new ProgenitorMimic(), "{4}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, copy);

        if (copy) {
            UUID warriorId = harness.getPermanentId(player2, "Kraul Warrior");
            harness.handlePermanentChosen(player1, warriorId);
        }

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Progenitor Mimic"))
                .findFirst().orElse(null);
    }

    private long controlledWarriors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kraul Warrior"))
                .count();
    }

    @Test
    @DisplayName("Enters as a copy of a creature and makes a token copy at the beginning of upkeep")
    void copiesCreatureAndTokensEachUpkeep() {
        Permanent mimic = copyKraulWarrior(true);
        assertThat(mimic).isNotNull();
        assertThat(mimic.getCard().getName()).isEqualTo("Kraul Warrior");
        assertThat(controlledWarriors()).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlledWarriors()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy it creates is a token, so it never triggers itself")
    void createdCopyIsAToken() {
        assertThat(copyKraulWarrior(true)).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kraul Warrior"))
                .filter(p -> p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenPlayerDeclines() {
        assertThat(copyKraulWarrior(false)).isNull();
        harness.assertInGraveyard(player1, "Progenitor Mimic");
    }

    @Test
    void tokenCopiesDoNotMultiplyOnLaterUpkeeps() {
        copyKraulWarrior(true);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(controlledWarriors()).isEqualTo(2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(controlledWarriors()).isEqualTo(3);
    }

    @Test
    void copyingAnotherMimicAddsASecondIndependentUpkeepAbility() {
        Permanent first = copyKraulWarrior(true);
        harness.castFromHand(player1, new ProgenitorMimic(), "{4}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(controlledWarriors()).isEqualTo(5);
    }

    @Test
    void copyingATokenDoesNotMakeTheNewMimicAToken() {
        Permanent first = copyKraulWarrior(true);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ProgenitorMimic(), "{4}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getId().equals(first.getId()))
                .filter(p -> p.getOriginalCard().getName().equals("Progenitor Mimic"))
                .findFirst().orElseThrow();
        assertThat(second.getCard().isToken()).isFalse();
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(3);
    }

    @Test
    void upkeepStillCreatesACopyWhenMimicDiesInResponse() {
        Permanent mimic = copyKraulWarrior(true);
        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, mimic.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Progenitor Mimic");
        harness.passBothPriorities();

        assertThat(controlledWarriors()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(p -> p.getCard().isToken());
    }

    @Test
    void diesWithoutAnyCreatureAvailableToCopy() {
        harness.castFromHand(player1, new ProgenitorMimic(), "{4}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Progenitor Mimic");
        harness.assertInGraveyard(player1, "Progenitor Mimic");
    }

    @Test
    void tokenCopyRetainsEveryColorOfTheCopiedCreature() {
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new BeetleformMage());
        harness.castFromHand(player1, new ProgenitorMimic(), "{4}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mage.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectiveColors(gd, token))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    void tokenCopiesActivatedAbilitiesButNotTemporaryPowerBoosts() {
        Permanent mimic = copyKraulWarrior(true);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 6);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.passBothPriorities();
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
    }
}
