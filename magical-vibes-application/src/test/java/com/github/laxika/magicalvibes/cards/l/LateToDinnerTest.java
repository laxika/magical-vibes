package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LateToDinner.class, GrizzlyBears.class, HolyDay.class})
class LateToDinnerTest extends BaseCardTest {

    @Test
    void returnsCreatureAndCreatesFoodToken() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LateToDinner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().isToken()).isTrue();
    }

    @Test
    void foodTokenCanBeSacrificedForLife() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LateToDinner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void cannotTargetNonCreatureCard() {
        HolyDay nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new LateToDinner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
