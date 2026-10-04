package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuriousAssault.class, GarrukWildspeaker.class, WildJhovall.class, Tremor.class})
class FuriousAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature deals 1 damage to the chosen player")
    void creatureSpellDealsDamage() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a creature deals 1 damage to a planeswalker target")
    void creatureSpellDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new FuriousAssault());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Furious Assault")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent casting a creature does not trigger Furious Assault")
    void opponentCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WildJhovall(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The creature's caster can target themselves with the mandatory trigger")
    void creatureSpellCanDamageController() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Furious Assault")
    void creatureEnteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new FuriousAssault());

        harness.enterBattlefieldAndReturn(player1, new WildJhovall());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The damage trigger cannot target a creature")
    void creatureIsNotALegalTarget() {
        harness.addToBattlefield(player1, new FuriousAssault());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WildJhovall());
        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
}
