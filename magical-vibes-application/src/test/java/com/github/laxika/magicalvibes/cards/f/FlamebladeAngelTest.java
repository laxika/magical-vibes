package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamebladeAngel.class, FieryTemper.class, DevilthornFox.class})
class FlamebladeAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent damage to the controller may be dealt back to its source's controller")
    void opponentDamageToControllerDealsDamageBackWhenAccepted() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent damage to a controlled permanent triggers the ability")
    void opponentDamageToControlledPermanentTriggers() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, fox.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage from your own source does not trigger the ability")
    void ownSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void optionalDamageCanBeDeclined() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }


    @Test
    void lethalDamageToAngelStillTriggersAndResolvesAfterItDies() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new FlamebladeAngel());
        harness.setHand(player2, List.of(new FieryTemper(), new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, angel.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castAndResolveInstant(player2, 0, angel.getId());
        harness.assertInGraveyard(player1, "Flameblade Angel");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
    }

    @Test
    void damageToOpponentDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCombatDamageToControllerTriggers() {
        harness.addToBattlefield(player1, new FlamebladeAngel());
        harness.addToBattlefield(player2, new DevilthornFox());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, Map.of());
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 19);
    }
}
