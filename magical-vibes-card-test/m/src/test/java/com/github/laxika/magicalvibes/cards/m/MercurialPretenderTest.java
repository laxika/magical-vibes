package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercurialPretender.class, RuneclawBear.class})
class MercurialPretenderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a copy of a creature its controller controls and gains the return ability")
    void copiesControlledCreatureWithReturnAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        addPretenderToHandAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent pretender = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mercurial Pretender"))
                .findFirst().orElseThrow();
        assertThat(pretender.getOriginalCard().getName()).isEqualTo("Mercurial Pretender");
        assertThat(pretender.getCard().getName()).isEqualTo("Runeclaw Bear");
        assertThat(pretender.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot copy a creature controlled by an opponent")
    void cannotCopyOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        addPretenderToHandAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownBears.getId());
    }

    @Test
    @DisplayName("The copied return ability returns Mercurial Pretender to its owner's hand")
    void copiedReturnAbilityBouncesPretender() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        addPretenderToHandAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent pretender = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mercurial Pretender"))
                .findFirst().orElseThrow();
        int pretenderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pretender);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, pretenderIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Mercurial Pretender"));
        harness.assertInHand(player1, "Mercurial Pretender");
    }

    @Test
    @DisplayName("Returning the copy is an effect that waits for the ability to resolve")
    void remainsOnBattlefieldUntilReturnAbilityResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        addPretenderToHandAndCast();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent pretender = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mercurial Pretender"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pretender), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pretender);
        harness.assertNotInHand(player1, "Mercurial Pretender");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pretender);
        harness.assertInHand(player1, "Mercurial Pretender");
    }

    @Test
    @DisplayName("Declining to copy leaves a zero-toughness creature that dies")
    void decliningCopyDies() {
        harness.addToBattlefield(player1, new RuneclawBear());
        addPretenderToHandAndCast();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Mercurial Pretender");
        harness.assertInGraveyard(player1, "Mercurial Pretender");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("With only an opponent's creature available, Pretender enters without copying and dies")
    void noControlledCreatureDiesWithoutCopying() {
        harness.addToBattlefield(player2, new RuneclawBear());
        addPretenderToHandAndCast();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mercurial Pretender");
        harness.assertInGraveyard(player1, "Mercurial Pretender");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    private void addPretenderToHandAndCast() {
        harness.castFromHand(player1, new MercurialPretender(), "{4}{U}");
    }
}
