package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraidedNet.class, BraidedQuipu.class, DarksteelRelic.class, GrizzlyBears.class,
        HillGiant.class, Island.class, LlanowarElves.class})
class BraidedNetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three net counters and locks another nonland permanent while tapped")
    void entersAndLocksAnotherPermanent() {
        harness.setHand(player1, List.of(new BraidedNet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent net = findPermanent(player1, "Braided Net");
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThat(net.getCounterCount(CounterType.NET)).isEqualTo(3);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(elves.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(net.getCounterCount(CounterType.NET)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns Braided Quipu transformed")
    void craftsIntoBraidedQuipu() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(net);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof BraidedQuipu);
    }

    @Test
    @DisplayName("Quipu draws for each artifact before going third from the top")
    void quipuDrawsAndReturnsToLibrary() {
        Permanent quipu = addTransformedQuipu();
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new LlanowarElves();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, quipu.getOriginalCard());
    }

    @Test
    @DisplayName("An already tapped opponent's artifact stays locked after Net leaves, until it untaps")
    void lockPersistsWithoutNetAndEndsOnUntap() {
        Permanent net = harness.enterBattlefieldAndReturn(player1, new BraidedNet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BraidedNet());
        harness.addToBattlefield(player2, new BraidedNet());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, net));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof BraidedQuipu);
    }

    @Test
    @DisplayName("Net cannot target itself or a land")
    void rejectsSelfAndLandTargets() {
        Permanent net = harness.enterBattlefieldAndReturn(player1, new BraidedNet());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, net.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(net.isTapped()).isFalse();
        assertThat(net.getCounterCount(CounterType.NET)).isEqualTo(3);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Net cannot activate its tap ability without a net counter")
    void cannotActivateWithoutCounter() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BraidedNet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(net.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Craft can exile an artifact card from the controller's graveyard")
    void craftsWithGraveyardArtifact() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Card material = new BraidedNet();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(net);
        assertThat(gd.findExiledCard(net.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(material);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(net.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof BraidedQuipu
                        && permanent.getCounterCount(CounterType.NET) == 0);
    }

    @Test
    @DisplayName("Craft cannot use its own source as the only material")
    void craftNeedsAnotherArtifact() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(net);
        assertThat(gd.findExiledCard(net.getOriginalCard().getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Craft is restricted to sorcery timing")
    void cannotCraftDuringCombat() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(net, material);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Quipu counts artifacts at resolution and returns exactly third from the top")
    void countsArtifactsAtResolutionAndReturnsThird() {
        Permanent quipu = addTransformedQuipu();
        harness.addToBattlefield(player2, new BraidedNet());
        List<Card> library = List.of(new BraidedNet(), new BraidedNet(), new BraidedNet(),
                new BraidedNet(), new BraidedNet());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new BraidedNet());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                library.get(2), library.get(3), quipu.getOriginalCard(), library.get(4));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(quipu);
    }

    @Test
    @DisplayName("Quipu's ability still draws when its source leaves before resolution")
    void drawsWithoutSourceAtResolution() {
        Permanent quipu = addTransformedQuipu();
        harness.addToBattlefield(player1, new BraidedNet());
        Card first = new BraidedNet();
        Card second = new BraidedNet();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, quipu));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(quipu.getOriginalCard(), first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Craft returns a borrowed Net under its owner's control")
    void craftReturnsUnderOwnersControl() {
        BraidedNet card = new BraidedNet();
        card.setOwnerId(player2.getId());
        Permanent net = harness.addToBattlefieldAndReturn(player1, card);
        harness.addToBattlefield(player1, new BraidedNet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.isTransformed()).isTrue();
            assertThat(permanent.getCard()).isInstanceOf(BraidedQuipu.class);
            assertThat(permanent.getOriginalCard()).isSameAs(net.getOriginalCard());
        });
    }

    @Test
    @DisplayName("A borrowed Quipu draws for its controller and goes into its owner's library")
    void quipuDrawsForControllerAndReturnsToOwnersLibrary() {
        Permanent quipu = addTransformedQuipu();
        quipu.getOriginalCard().setOwnerId(player2.getId());
        Card drawn = new BraidedNet();
        Card remaining = new BraidedNet();
        List<Card> ownerLibrary = List.of(new BraidedNet(), new BraidedNet(), new BraidedNet());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setLibrary(player2, ownerLibrary);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(
                ownerLibrary.get(0), ownerLibrary.get(1), quipu.getOriginalCard(), ownerLibrary.get(2));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(quipu);
    }

    private Permanent addTransformedQuipu() {
        BraidedNet front = new BraidedNet();
        Permanent quipu = new Permanent(front);
        quipu.setCard(front.getBackFaceCard());
        quipu.setTransformed(true);
        quipu.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(quipu);
        return quipu;
    }
}
