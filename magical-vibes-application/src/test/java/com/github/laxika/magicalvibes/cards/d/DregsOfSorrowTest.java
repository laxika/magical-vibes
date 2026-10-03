package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DregsOfSorrow.class, BogWraith.class, Boomerang.class, GrizzlyBears.class, HillGiant.class,
        Regeneration.class, Swamp.class})
class DregsOfSorrowTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two nonblack creatures and draws two cards")
    void destroysXCreaturesAndDrawsX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=2: {2}{4}{B} = 7

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castSorcery(player1, 0, 2, List.of(bears.getId(), giant.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("X=0 destroys nothing and draws nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=0: {0}{4}{B} = 5

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Still draws X even when a target is removed before resolution")
    void drawsXEvenWhenTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=2

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castSorcery(player1, 0, 2, List.of(bears.getId(), giant.getId()));

        // One target leaves before resolution — the spell still draws X.
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target more creatures than X")
    void cannotTargetMoreThanX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(bears.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target fewer creatures than X")
    void cannotTargetFewerThanX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new BogWraith());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(blackCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Can target a nonblack creature you control")
    void canTargetOwnNonblackCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1: {1}{4}{B} = 6

        harness.castAndResolveSorcery(player1, 0, 1, ownCreature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creatures");
    }

    @Test
    @DisplayName("Draws no cards when the only target leaves before resolution")
    void doesNotDrawWhenAllTargetsAreIllegal() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 1, List.of(bears.getId()));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dregs of Sorrow");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice for X=2")
    void cannotChooseDuplicateTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration prevents destruction but does not reduce the number of cards drawn")
    void drawsXWhenOneCreatureRegenerates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Regeneration());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 2, List.of(bears.getId(), giant.getId()));
        harness.activateAbility(player2, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
