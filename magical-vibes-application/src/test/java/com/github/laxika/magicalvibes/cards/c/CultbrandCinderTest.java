package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScuzzbackScrapper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CultbrandCinder.class, AirElemental.class, GrizzlyBears.class, ScuzzbackScrapper.class})
class CultbrandCinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();

        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent target = findPermanent(player2, "Air Elemental");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB counter shrinks a 2/2 to 1/1")
    void etbCounterShrinksSmallCreature() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 2/2 with one -1/-1 counter survives as 1/1
        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB → fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Must target itself when it is the only creature")
    void mustTargetItselfOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent cinder = findPermanent(player1, "Cultbrand Cinder");
        harness.handlePermanentChosen(player1, cinder.getId());
        harness.passBothPriorities();

        assertThat(cinder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(cinder.getEffectivePower()).isEqualTo(2);
        assertThat(cinder.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast can put a counter on its controller's creature")
    void enteringWithoutCastingCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CultbrandCinder());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new CultbrandCinder());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A counter kills a creature with one toughness")
    void counterKillsOneToughnessCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScuzzbackScrapper());
        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scuzzback Scrapper");
        harness.assertInGraveyard(player2, "Scuzzback Scrapper");
        harness.assertOnBattlefield(player1, "Cultbrand Cinder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger resolves even if Cultbrand Cinder leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CultbrandCinder());
        harness.setHand(player1, List.of(new CultbrandCinder()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
