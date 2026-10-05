package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StokeTheFlames;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoyfulStormsculptor.class, GrizzlyBears.class, InvasionOfKamigawa.class,
        Shock.class, StokeTheFlames.class})
class JoyfulStormsculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two blue and red Elemental tokens")
    void createsElementalTokens() {
        harness.castFromHand(player1, new JoyfulStormsculptor(), "{3}{U}{R}");
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Elemental");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
    }

    @Test
    @DisplayName("Casting a convoke spell damages each opponent and each battle they protect")
    void convokeSpellDamagesOpponentsAndProtectedBattles() {
        harness.addToBattlefield(player1, new JoyfulStormsculptor());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);
        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a spell without convoke does not trigger the damage ability")
    void nonConvokeSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JoyfulStormsculptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A convoke spell paid entirely with mana triggers before the spell resolves")
    void convokeSpellTriggersWithoutTappingCreatures() {
        harness.addToBattlefield(player1, new JoyfulStormsculptor());
        Permanent protectedByOpponent = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        protectedByOpponent.setProtectorPlayerId(player2.getId());
        protectedByOpponent.setCounterCount(CounterType.DEFENSE, 4);
        Permanent protectedByController = harness.addToBattlefieldAndReturn(player2, new InvasionOfKamigawa());
        protectedByController.setProtectorPlayerId(player1.getId());
        protectedByController.setCounterCount(CounterType.DEFENSE, 4);
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(protectedByOpponent.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        assertThat(protectedByController.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The damage trigger still resolves after Joyful Stormsculptor leaves the battlefield")
    void damageTriggerSurvivesSourceRemoval() {
        Permanent sculptor = harness.addToBattlefieldAndReturn(player1, new JoyfulStormsculptor());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.setHand(player2, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, sculptor.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Joyful Stormsculptor");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's convoke spell does not trigger the damage ability")
    void opponentConvokeSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JoyfulStormsculptor());
        harness.setHand(player2, List.of(new StokeTheFlames()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
