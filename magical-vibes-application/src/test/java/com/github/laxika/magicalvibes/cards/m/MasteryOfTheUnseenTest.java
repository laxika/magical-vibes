package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasteryOfTheUnseen.class, GrizzlyBears.class, BreakOpen.class})
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

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Turning a manifested enchantment face up triggers each Mastery, including itself")
    void gainsLifeWhenNoncreatureAndMasteryItselfTurnFaceUp() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MasteryOfTheUnseen()));
        addManifestMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(manifested.isFaceDown()).isTrue();
        harness.setHand(player2, List.of(new BreakOpen()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, manifested.getId());

        assertThat(manifested.isFaceDown()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Manifesting from an empty library does nothing")
    void emptyLibraryDoesNotCreatePermanent() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.setLibrary(player1, List.of());
        addManifestMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent turning a creature face up does not trigger your Mastery")
    void opponentTurningCreatureFaceUpDoesNotGainLife() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MasteryOfTheUnseen());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.turnFaceUp(player2, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Life gain counts creatures on resolution and survives the turned creature leaving")
    void countsCreaturesOnResolutionAfterTurnedCreatureLeaves() {
        harness.addToBattlefield(player1, new MasteryOfTheUnseen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManifestMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 2);
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(manifested);
        harness.assertLife(player1, 21);
    }

    private void addManifestMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
