package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card of its controller's library")
    void manifestsTopCard() {
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSoulSummonsMana();

        harness.castSorcery(player1, 0, 0);
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
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSoulSummonsMana();

        harness.castSorcery(player1, 0, 0);
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
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of());
        addSoulSummonsMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isFaceDown);
    }

    private Permanent findManifestedPermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }

    private void addSoulSummonsMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
