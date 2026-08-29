package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercilessResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature as an additional cost draws two cards")
    void sacrificesCreatureAndDrawsTwoCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        harness.setHand(player1, List.of(new MercilessResolve()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land");
    }
}
