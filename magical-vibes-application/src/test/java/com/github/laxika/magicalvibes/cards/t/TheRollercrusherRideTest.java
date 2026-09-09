package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FallOfTheHammer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRollercrusherRide.class, Shock.class, FallOfTheHammer.class,
        GrizzlyBears.class, AirElemental.class, MindStone.class, Forest.class})
class TheRollercrusherRideTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each of up to X target creatures")
    void dealsXDamageToEachOfUpToXTargetCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> creatures = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.handlePermanentChosen(player1, creatures.get(0).getId());
        harness.handlePermanentChosen(player1, creatures.get(1).getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not double noncombat damage without delirium")
    void doesNotDoubleNoncombatDamageWithoutDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles noncombat damage with delirium")
    void doublesNoncombatDamageWithDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles damage dealt by a creature you control with delirium")
    void doublesDamageDealtByControlledCreatureWithDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }
}
