package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.e.Extinguish;
import com.github.laxika.magicalvibes.cards.s.StrategicPlanning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticDenial.class, ForestBear.class, StrategicPlanning.class, Extinguish.class})
class MysticDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        ForestBear bears = new ForestBear();

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertNotOnBattlefield(player1, "Forest Bear");
    }

    @Test
    @DisplayName("Counters a sorcery spell")
    void countersSorcerySpell() {
        StrategicPlanning strategicPlanning = new StrategicPlanning();

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, strategicPlanning, "{1}{U}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, strategicPlanning.getId());

        harness.assertInGraveyard(player1, "Strategic Planning");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        StrategicPlanning brilliance = new StrategicPlanning();
        harness.castFromHand(player1, brilliance, "{1}{U}");

        Extinguish extinguish = new Extinguish();
        harness.setHand(player1, List.of(extinguish));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, brilliance.getId());

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, extinguish.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ForestBear());

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target spell leaves the stack before resolution")
    void fizzlesIfTargetSpellRemoved() {
        ForestBear bears = new ForestBear();

        MysticDenial mysticDenial = new MysticDenial();
        harness.setHand(player2, List.of(mysticDenial));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Mystic Denial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's own creature spell")
    void countersOwnCreatureSpell() {
        ForestBear bear = new ForestBear();
        harness.castFromHand(player1, bear, "{1}{G}");

        harness.setHand(player1, List.of(new MysticDenial()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertInGraveyard(player1, "Mystic Denial");
        harness.assertNotOnBattlefield(player1, "Forest Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not resolve when another counterspell removes its target")
    void targetCounteredInResponse() {
        StrategicPlanning planning = new StrategicPlanning();
        harness.castFromHand(player1, planning, "{1}{U}");
        harness.setHand(player1, List.of(new Extinguish()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, planning.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, planning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Strategic Planning");
        harness.assertInGraveyard(player1, "Extinguish");
        harness.assertInGraveyard(player2, "Mystic Denial");
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
