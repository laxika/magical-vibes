package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommonBlackRemoval.class, GrizzlyBears.class, HillGiant.class, Spellbook.class,
        DrudgeSkeletons.class, Unsummon.class, GiantGrowth.class})
class CommonBlackRemovalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates a Food token")
    void createsFoodToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Destroys a creature and creates a Treasure token")
    void createsTreasureToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Destroys a creature and puts a menace counter on a creature you control")
    void putsMenaceCounterOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target, 2);

        assertThat(ownCreature.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a creature and its controller mills cards equal to its power")
    void millsDestroyedCreaturesControllerByPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        cast(target, 3);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new CommonBlackRemoval()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosesMenaceRecipientDuringResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast(target, 2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(second.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotDeclineMandatoryMenaceCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        cast(target, 2);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(first.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    void menaceModeStillDestroysWithoutAnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        cast(target, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Spellbook").getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalTargetPreventsTokenCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CommonBlackRemoval()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Common Black Removal");
    }

    @Test
    void createsTreasureEvenWhenCreatureRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        cast(target, 1);

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    void millsUsingModifiedPowerBeforeDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook(), new Spellbook(),
                new Spellbook(), new Spellbook(), new Spellbook(), new Spellbook()));
        int ownLibraryBefore = gd.playerDecks.get(player1.getId()).size();
        cast(target, 3);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Spellbook"))).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibraryBefore);
    }

    @Test
    void negativePowerMillsNoCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(-3);
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        cast(target, 3);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
    }

    @Test
    void targetingOwnCreatureMillsItsControllerAndOnlyAvailableCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Spellbook(), new Spellbook()));
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();
        cast(target, 3);

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore);
    }

    private void cast(Permanent target, int mode) {
        harness.setHand(player1, List.of(new CommonBlackRemoval()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
