package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class ChandrasPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand when a red instant you control damages an opponent")
    void redInstantDamageToOpponentReturnsPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Shock resolves, trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        harness.assertInHand(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Does not return when the red spell damages a creature instead of a player")
    void redInstantDamageToCreatureDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    @Test
    @DisplayName("Does not return when a nonred spell damages an opponent")
    void nonRedSpellDamageDoesNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new ChandrasPhoenix()));
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra's Phoenix");
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = new Permanent(new ChandraNalaar());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
