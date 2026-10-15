package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoodedHydra;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulSummons.class, GrizzlyBears.class, Forest.class, HoodedHydra.class})
class SoulSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card of its controller's library")
    void manifestsTopCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent manifested = findManifestedPermanent();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A manifested creature can turn face up for its mana cost")
    void manifestedCreatureTurnsFaceUp() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent manifested = findManifestedPermanent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when its controller's library is empty")
    void doesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isFaceDown);
    }

    @Test
    @DisplayName("A manifested creature cannot turn face up without paying its mana cost")
    void manifestedCreatureRequiresMana() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent manifested = findManifestedPermanent();
        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
    }

    @Test
    @DisplayName("A manifested sorcery cannot turn face up even when its mana cost is available")
    void manifestedSorceryCannotTurnFaceUp() {
        harness.setLibrary(player1, List.of(new SoulSummons()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent manifested = findManifestedPermanent();
        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent findManifestedPermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("A manifested land is a 2/2 creature but cannot turn face up using manifest")
    void manifestedLandCannotTurnFaceUp() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent manifested = findManifestedPermanent();
        assertThat(gqs.isCreature(gd, manifested)).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
    }

    @Test
    @DisplayName("Manifests only the controller's top card and preserves the remaining library order")
    void leavesOtherLibraryCardsAlone() {
        Forest top = new Forest();
        SoulSummons second = new SoulSummons();
        Forest third = new Forest();
        Forest opponentTop = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(findManifestedPermanent().getCard()).isSameAs(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A manifested X-cost creature turns face up with X fixed at zero")
    void manifestedXCostCreatureTurnsFaceUpWithoutChoosingX() {
        harness.setLibrary(player1, List.of(new HoodedHydra()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent manifested = findManifestedPermanent();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(5);
    }
}
