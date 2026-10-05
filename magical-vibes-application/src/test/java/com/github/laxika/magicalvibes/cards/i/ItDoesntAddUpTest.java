package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ItDoesntAddUp.class, GrizzlyBears.class, HolyDay.class})
class ItDoesntAddUpTest extends BaseCardTest {

    @Test
    @DisplayName("Returns and suspects a target creature card from your graveyard")
    void returnsAndSuspectsTargetCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ItDoesntAddUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, returned)).isFalse();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ItDoesntAddUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ItDoesntAddUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another creature when the target leaves the graveyard")
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new ItDoesntAddUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        harness.assertInGraveyard(player1, "It Doesn't Add Up");
        assertThat(gd.stack).isEmpty();
    }
}
