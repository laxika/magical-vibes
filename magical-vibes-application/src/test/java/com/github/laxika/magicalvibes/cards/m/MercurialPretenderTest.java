package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercurialPretenderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a copy of a creature its controller controls and gains the return ability")
    void copiesControlledCreatureWithReturnAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addPretenderToHandAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent pretender = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mercurial Pretender"))
                .findFirst().orElseThrow();
        assertThat(pretender.getOriginalCard().getName()).isEqualTo("Mercurial Pretender");
        assertThat(pretender.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(pretender.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot copy a creature controlled by an opponent")
    void cannotCopyOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mercurial Pretender"));
    }

    private void addPretenderToHandAndCast() {
        harness.setHand(player1, List.of(new MercurialPretender()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
