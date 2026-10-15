package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.p.PrismaticLace;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasPhoenix.class, Shock.class, ConsumeSpirit.class, RuneclawBear.class,
        ChandraTheFirebrand.class, LavaAxe.class})
class ChandrasPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand when a red instant you control damages an opponent")
    void redInstantDamageToOpponentReturnsPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId()); // trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        harness.assertInHand(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Does not return when the red spell damages a creature instead of a player")
    void redInstantDamageToCreatureDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Does not return when a nonred spell damages an opponent")
    void nonRedSpellDamageDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Returns to hand when a red planeswalker you control damages an opponent")
    void redPlaneswalkerDamageReturnsPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        addReadyChandra(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Does not return when an opponent's red spell damages you")
    void opponentRedSpellDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    @Test
    void redSorceryReturnsEachPhoenixFromItsOwnersGraveyard() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix(), new ChandrasPhoenix()));
        harness.setGraveyard(player2, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof ChandrasPhoenix).hasSize(2);
        harness.assertNotInGraveyard(player1, "Chandra's Phoenix");
        harness.assertInGraveyard(player2, "Chandra's Phoenix");
    }

    @Test
    void damageToYourselfDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    @Test
    @CardUsed({PrismaticLace.class})
    void planeswalkerThatBecameBlueDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        Permanent chandra = addReadyChandra(player1);
        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraTheFirebrand());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
