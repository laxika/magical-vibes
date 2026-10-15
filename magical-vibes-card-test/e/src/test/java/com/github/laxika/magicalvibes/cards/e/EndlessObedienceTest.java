package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndlessObedience.class, GrizzlyBears.class, HolyDay.class, BlackCat.class})
class EndlessObedienceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from your graveyard to the battlefield under your control")
    void returnsCreatureFromOwnGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new EndlessObedience()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a target creature card from an opponent's graveyard under your control")
    void returnsCreatureFromOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new EndlessObedience()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new EndlessObedience()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void convokePaysBlackAndGenericManaWithSummoningSickCreatures() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new EndlessObedience()));
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new BlackCat());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cat.setSummoningSick(true);
        bear.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(cat.getId(), bear.getId()));

        assertThat(cat.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(target.getId())).hasSize(1);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        Card other = new BlackCat();
        harness.setGraveyard(player2, List.of(target, other));
        harness.setHand(player1, List.of(new EndlessObedience()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Black Cat");
        harness.assertInGraveyard(player2, "Black Cat");
        harness.assertInGraveyard(player1, "Endless Obedience");
        assertThat(gd.stack).isEmpty();
    }
}
