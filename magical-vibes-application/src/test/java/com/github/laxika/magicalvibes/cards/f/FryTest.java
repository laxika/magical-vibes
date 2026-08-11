package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a white creature")
    void dealsDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 5 damage to a blue planeswalker")
    void dealsDamageToBluePlaneswalker() {
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not white or blue")
    void cannotTargetNonWhiteOrBlueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new EliteVanguard());
        Fry fry = new Fry();
        harness.setHand(player1, List.of(fry));
        addFryMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fry.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void addFryMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
