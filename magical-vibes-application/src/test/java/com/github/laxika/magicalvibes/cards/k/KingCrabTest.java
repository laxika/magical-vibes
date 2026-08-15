package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KingCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target green creature on top of its owner's library")
    void putsTargetGreenCreatureOnTopOfOwnersLibrary() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new KingCrab());
        crab.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, crabIndex, null, bears.getId());
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(deckSizeBefore + 1);
        assertThat(library.getFirst().getId()).isEqualTo(bears.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new KingCrab());
        crab.setSummoningSick(false);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, crabIndex, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the target is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new KingCrab());
        crab.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, crabIndex, null, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }
}
