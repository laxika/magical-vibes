package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThisIsHowItEnds.class, GrizzlyBears.class, Forest.class, TheValeyard.class})
class ThisIsHowItEndsTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles the target, then its owner may choose to lose 5 life")
    void shufflesTargetThenOwnerCanChooseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly(ThisIsHowItEnds.LOSE_LIFE_OPTION,
                        ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The owner may choose another creature they own to shuffle in")
    void ownerCanShuffleAnotherOwnedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstOther.getId(), secondOther.getId());
        harness.handlePermanentChosen(player2, firstOther.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId())
                        || permanent.getId().equals(firstOther.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondOther.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The shuffle option may be chosen with no other creature owned")
    void canChooseToShuffleWhenNoOtherCreatureIsOwned() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The only other owned creature is shuffled in without a prompt")
    void automaticallyShufflesTheOnlyOtherOwnedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The target's owner, not its controller, faces the choice")
    void ownerRatherThanControllerFacesTheChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.LOSE_LIFE_OPTION);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A creature the owner owns but another player controls can be shuffled in")
    void canShuffleAnOwnedCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(unrelated);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Valeyard repeats the choice after a creature was chosen")
    void valeyardRepeatsChoiceAfterChoosingAmongMultipleCreatures() {
        harness.addToBattlefield(player1, new TheValeyard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ThisIsHowItEnds.SHUFFLE_CREATURE_OPTION);
        harness.handlePermanentChosen(player2, firstOther.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player2,
                ThisIsHowItEnds.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 15);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(secondOther);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ThisIsHowItEnds()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new ThisIsHowItEnds()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
