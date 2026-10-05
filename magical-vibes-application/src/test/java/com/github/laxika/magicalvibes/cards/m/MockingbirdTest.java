package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PondProphet;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mockingbird.class, GrizzlyBears.class, HillGiant.class, BraveKinDuo.class,
        Ornithopter.class, PondProphet.class, Zombify.class, HangarbackWalker.class})
class MockingbirdTest extends BaseCardTest {

    @Test
    @DisplayName("Uses total mana spent, restricts the copy choice, and keeps Bird and flying")
    void copiesCreatureWithinManaSpentLimit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mockingbird mockingbird = new Mockingbird();
        harness.setHand(player1, List.of(mockingbird));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(bears.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(hillGiant.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Mockingbird"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("May decline copying an eligible creature")
    void mayDeclineCopy() {
        harness.addToBattlefield(player2, new BraveKinDuo());
        harness.setHand(player1, List.of(new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Mockingbird");
        harness.assertNotOnBattlefield(player1, "Brave-Kin Duo");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X zero still allows a one-mana creature, without copying its counters or tapped state")
    void copiesOwnOneManaCreatureForZeroX() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        duo.tap();
        duo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, duo.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(duo.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Brave-Kin Duo");
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BIRD, CardSubtype.RABBIT, CardSubtype.MOUSE);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters normally when no creature is within the mana-spent limit")
    void entersWithoutEligibleCopy() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mockingbird");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The copied creature's enters ability triggers")
    void copiedEntersAbilityTriggers() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player2, new PondProphet());
        BraveKinDuo drawnCard = new BraveKinDuo();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, prophet.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pond Prophet");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Copying a copy uses its copied mana value and keeps its copy exceptions")
    void copiesAnotherCopy() {
        Permanent duo = harness.addToBattlefieldAndReturn(player2, new BraveKinDuo());
        harness.setHand(player1, List.of(new Mockingbird(), new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, duo.getId());
        Permanent firstCopy = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        Permanent secondCopy = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(secondCopy.getCard().getName()).isEqualTo("Brave-Kin Duo");
        assertThat(secondCopy.getCard().getSubtypes()).contains(CardSubtype.BIRD, CardSubtype.RABBIT, CardSubtype.MOUSE);
        assertThat(gqs.hasKeyword(gd, secondCopy, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Reanimated Mockingbird may copy a zero-mana creature")
    void reanimatedMockingbirdCanCopyZeroManaCreature() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new BraveKinDuo());
        Mockingbird mockingbird = new Mockingbird();
        harness.setGraveyard(player1, List.of(mockingbird));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, mockingbird.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ornithopter.getId());
        harness.handlePermanentChosen(player1, ornithopter.getId());

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Mockingbird");
    }

    @Test
    @DisplayName("Copying a creature with X in its mana cost uses zero for its entry counters")
    void copiedCreatureDoesNotUseMockingbirdsX() {
        Permanent walker = harness.addToBattlefieldAndReturn(player2, new HangarbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Mockingbird()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, walker.getId());

        harness.assertNotOnBattlefield(player1, "Hangarback Walker");
        harness.assertInGraveyard(player1, "Mockingbird");
        harness.assertOnBattlefield(player2, "Hangarback Walker");
    }
}
