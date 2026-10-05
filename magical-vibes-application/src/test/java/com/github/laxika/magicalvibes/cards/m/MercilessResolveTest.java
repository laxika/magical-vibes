package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
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

@CardUsed({MercilessResolve.class, DrownyardExplorers.class, Swamp.class, MagnifyingGlass.class})
class MercilessResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature as an additional cost draws two cards")
    void sacrificesCreatureAndDrawsTwoCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrownyardExplorers());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drownyard Explorers");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Sacrificing a land as an additional cost draws two cards")
    void sacrificesLandAndDrawsTwoCards() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot cast without a creature or land to sacrifice")
    void cannotCastWithoutCreatureOrLandToSacrifice() {
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature nonland permanent")
    void cannotSacrificeNonCreatureNonlandPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land");
    }

    @Test
    @DisplayName("The sacrifice is paid before the spell resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrownyardExplorers());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());

        harness.assertNotOnBattlefield(player1, "Drownyard Explorers");
        harness.assertInGraveyard(player1, "Drownyard Explorers");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Merciless Resolve");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's permanent")
    void cannotSacrificeOpponentsPermanent() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrownyardExplorers());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");

        harness.assertOnBattlefield(player2, "Drownyard Explorers");
        harness.assertInHand(player1, "Merciless Resolve");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land can be sacrificed")
    void canSacrificeTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        land.setTapped(true);
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
