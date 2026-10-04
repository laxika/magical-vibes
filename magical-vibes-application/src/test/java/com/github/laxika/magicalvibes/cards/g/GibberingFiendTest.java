package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GibberingFiend.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class,
        WitchbaneOrb.class})
class GibberingFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and deals 1 damage to each opponent")
    void dealsDamageToEachOpponentOnEnter() {
        harness.setHand(player1, List.of(new GibberingFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Delirium deals 1 damage to an opponent during that opponent's upkeep")
    void dealsDamageOnOpponentsUpkeepWithDelirium() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not deal damage during an opponent's upkeep without delirium")
    void doesNotDealDamageWithoutDelirium() {
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Rechecks delirium when the triggered ability resolves")
    void rechecksDeliriumAtResolution() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Delirium upkeep damage affects opponents with hexproof")
    void upkeepDamageDoesNotTargetOpponent() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());
        harness.addToBattlefield(player2, new WitchbaneOrb());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Entering damage affects opponents with hexproof and spares the controller")
    void enteringDamageDoesNotTargetOpponent() {
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setHand(player1, List.of(new GibberingFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Four cards with only three card types do not enable delirium")
    void duplicateCardTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Shock()));
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable the controller's delirium")
    void opponentGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
