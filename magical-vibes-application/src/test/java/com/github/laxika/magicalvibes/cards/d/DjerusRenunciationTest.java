package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjerusRenunciation.class, Forest.class, GrizzlyBears.class})
class DjerusRenunciationTest extends BaseCardTest {

    @Test
    @DisplayName("Taps both target creatures")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May tap a single creature (up to two)")
    void tapsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Djeru's Renunciation");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May be cast with no targets even when creatures are present")
    void mayChooseNoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Djeru's Renunciation");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Djeru's Renunciation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotDuplicateTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps the remaining creature if the other target leaves")
    void resolvesWithOneRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.setGraveyard(player1, List.of(first.getCard()));
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Djeru's Renunciation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling pays the discard cost immediately and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Djeru's Renunciation");
        harness.assertNotInHand(player1, "Djeru's Renunciation");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with mana of the wrong color")
    void cyclingRequiresWhiteMana() {
        harness.setHand(player1, List.of(new DjerusRenunciation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Djeru's Renunciation");
        harness.assertNotInGraveyard(player1, "Djeru's Renunciation");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
