package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShinewendTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter")
    void entersWithPlusOneCounter() {
        harness.setHand(player1, List.of(new Shinewend()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shinewend = findPermanent(player1, "Shinewend");
        assertThat(shinewend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability removes a +1/+1 counter and destroys target enchantment")
    void abilityDestroysEnchantment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Shinewend());
        Permanent shinewend = findPermanent(player1, "Shinewend");
        shinewend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addToBattlefield(player2, new AngelicChorus());
        Permanent enchantment = findPermanent(player2, "Angelic Chorus");

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(shinewend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot activate the ability without a +1/+1 counter to remove")
    void cannotActivateWithoutCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Shinewend());
        Permanent shinewend = findPermanent(player1, "Shinewend");
        shinewend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.addToBattlefield(player2, new AngelicChorus());
        Permanent enchantment = findPermanent(player2, "Angelic Chorus");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Shinewend());
        Permanent shinewend = findPermanent(player1, "Shinewend");
        shinewend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
