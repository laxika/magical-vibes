package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HiddenGibbons;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KingCrab.class, YavimayaWurm.class, HiddenGibbons.class})
class KingCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target green creature on top of its owner's library")
    void putsTargetGreenCreatureOnTopOfOwnersLibrary() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, crabIndex, null, wurm.getId());
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(deckSizeBefore + 1);
        assertThat(library.getFirst().getId()).isEqualTo(wurm.getCard().getId());
        assertThat(crab.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Yavimaya Wurm");
        harness.assertNotInGraveyard(player2, "Yavimaya Wurm");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent target = addCreatureReady(player2, new KingCrab());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, crabIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNonCreaturePermanent() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent gibbons = harness.addToBattlefieldAndReturn(player2, new HiddenGibbons());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, crabIndex, null, gibbons.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing if the target is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        int crabIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, crabIndex, null, wurm.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getId().equals(wurm.getCard().getId()));
    }
}
