package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.w.WishcoinCrab;
import com.github.laxika.magicalvibes.cards.r.RadicalIdea;
import com.github.laxika.magicalvibes.cards.d.DimirInformant;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvertInvent.class, DirectCurrent.class, WishcoinCrab.class, RadicalIdea.class,
        DimirInformant.class, ObNixilisUnshackled.class})
class InvertInventTest extends BaseCardTest {

    private static final int INVERT = 0;
    private static final int INVENT = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Invert switches the power and toughness of up to two target creatures")
    void invertSwitchesTwoCreatures() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new DimirInformant());
        int tortoisePower = gqs.getEffectivePower(gd, tortoise);
        int tortoiseToughness = gqs.getEffectiveToughness(gd, tortoise);
        int wallPower = gqs.getEffectivePower(gd, wall);
        int wallToughness = gqs.getEffectiveToughness(gd, wall);

        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, INVERT, List.of(tortoise.getId(), wall.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(tortoiseToughness);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(tortoisePower);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(wallToughness);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(wallPower);
    }

    @Test
    @DisplayName("Invert can be cast with no targets")
    void invertCanChooseNoTargets() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, INVERT, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invert cannot target a player")
    void invertCannotTargetPlayer() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, INVERT, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Invent searches for an instant and a sorcery and puts them into hand")
    void inventSearchesForBothCardTypes() {
        harness.setLibrary(player1, List.of(new RadicalIdea(), new DirectCurrent(), new DimirInformant()));
        castInvent();

        PendingInteraction.LibrarySearch instantSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(instantSearch).isNotNull();
        assertThat(instantSearch.params().cards()).allMatch(card -> card.hasType(CardType.INSTANT));
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch sorcerySearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(sorcerySearch).isNotNull();
        assertThat(sorcerySearch.params().cards()).allMatch(card -> card.hasType(CardType.SORCERY));
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Radical Idea");
        harness.assertInHand(player1, "Direct Current");
    }

    @Test
    @DisplayName("Both halves cannot be cast together because the card has no fuse ability")
    void cannotCastBothHalvesTogether() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(tortoise.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Invert can switch one creature you control and expires at cleanup")
    void invertSingleTargetExpires() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new WishcoinCrab());
        int power = gqs.getEffectivePower(gd, crab);
        int toughness = gqs.getEffectiveToughness(gd, crab);
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, INVERT, List.of(crab.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(toughness);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(power);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Invert rejects more than two creature targets")
    void invertRejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, INVERT,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Invent can find only an instant when no sorcery exists")
    void inventFindsOnlyInstant() {
        harness.setLibrary(player1, List.of(new RadicalIdea(), new WishcoinCrab()));
        castInvent();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Radical Idea");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invent can find only a sorcery when no instant exists")
    void inventFindsOnlySorcery() {
        harness.setLibrary(player1, List.of(new DirectCurrent(), new WishcoinCrab()));
        castInvent();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Direct Current");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invent can decline both finds even when matching cards exist")
    void inventCanFindNeither() {
        harness.setLibrary(player1, List.of(new RadicalIdea(), new DirectCurrent()));
        castInvent();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Radical Idea");
        harness.assertNotInHand(player1, "Direct Current");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invent can decline the instant and still find the sorcery")
    void inventCanDeclineOnlyInstant() {
        harness.setLibrary(player1, List.of(new RadicalIdea(), new DirectCurrent()));
        castInvent();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Radical Idea");
        harness.assertInHand(player1, "Direct Current");
    }

    @Test
    @DisplayName("Invent resolves normally with an empty library")
    void inventWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castInvent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Invert // Invent");
    }

    @Test
    @DisplayName("Invent searches once even when finding both an instant and a sorcery")
    void inventTriggersSearchAbilityOnlyOnce() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 30);
        harness.setLibrary(player1, List.of(new RadicalIdea(), new DirectCurrent()));
        castInvent();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invert still switches the remaining creature when one target leaves")
    void invertResolvesForRemainingTarget() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new WishcoinCrab());
        Permanent informant = harness.addToBattlefieldAndReturn(player2, new DimirInformant());
        int power = gqs.getEffectivePower(gd, crab);
        int toughness = gqs.getEffectiveToughness(gd, crab);
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, INVERT, List.of(crab.getId(), informant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(informant);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(toughness);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(power);
    }

    @Test
    @DisplayName("Invent requires its own six-mana cost")
    void inventCannotUseInvertCost() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, INVENT, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castInvent() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalInstant(player1, 0, INVENT, List.of());
        harness.passBothPriorities();
    }
}
