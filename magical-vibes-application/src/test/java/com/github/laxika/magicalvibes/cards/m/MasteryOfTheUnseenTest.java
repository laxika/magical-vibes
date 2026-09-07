package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasteryOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card of its controller's library")
    void manifestsTopCard() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManifestMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gains life for each creature when a creature you control is turned face up")
    void gainsLifeForEachCreatureWhenCreatureTurnsFaceUp() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManifestMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    private void addManifestMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
