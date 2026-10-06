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
    void canTargetOwnGreenCreature() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent wurm = addCreatureReady(player1, new YavimayaWurm());
        harness.setLibrary(player1, List.of(new HiddenGibbons()));
        Card previousTop = gd.playerDecks.get(player1.getId()).getFirst();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wurm.getCard(), previousTop);
        harness.assertNotOnBattlefield(player1, "Yavimaya Wurm");
        assertThat(crab.isTapped()).isTrue();
    }

    @Test
    void abilityResolvesAfterCrabLeavesBattlefield() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, wurm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(crab);
        gd.playerGraveyards.get(player1.getId()).add(crab.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(wurm.getCard());
        harness.assertNotOnBattlefield(player2, "Yavimaya Wurm");
    }

    @Test
    void putsCreatureInOwnersLibraryWhenControlledByOpponent() {
        addCreatureReady(player1, new KingCrab());
        YavimayaWurm card = new YavimayaWurm();
        card.setOwnerId(player1.getId());
        Permanent wurm = addCreatureReady(player2, card);
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(card);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        harness.assertNotOnBattlefield(player2, "Yavimaya Wurm");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        crab.setSummoningSick(true);
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crab.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        crab.tap();
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent crab = addCreatureReady(player1, new KingCrab());
        Permanent wurm = addCreatureReady(player2, new YavimayaWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crab.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

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
