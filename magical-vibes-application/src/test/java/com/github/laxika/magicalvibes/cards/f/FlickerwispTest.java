package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flickerwisp.class, NipGwyllion.class, FloodedGrove.class, LayClaim.class})
class FlickerwispTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell triggers ETB exile ability targeting the permanent")
    void resolvingTriggersEtb() {
        harness.addToBattlefield(player2, new NipGwyllion());
        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Nip Gwyllion");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flickerwisp");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB exiles the target permanent, which returns at next end step under owner's control")
    void exilesAndReturnsAtEndStep() {
        harness.addToBattlefield(player2, new NipGwyllion());
        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Nip Gwyllion");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell + ETB
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nip Gwyllion");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Nip Gwyllion"));

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Nip Gwyllion");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Nip Gwyllion"));
    }

    @Test
    @DisplayName("Can exile own permanent and it returns under its owner's control")
    void canExileOwnPermanent() {
        harness.addToBattlefield(player1, new NipGwyllion());
        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Nip Gwyllion");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Nip Gwyllion");
    }

    @Test
    @DisplayName("Can exile a noncreature permanent")
    void canExileNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FloodedGrove());
        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Flooded Grove");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flooded Grove");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Flooded Grove"));

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Flooded Grove");
    }

    @Test
    @DisplayName("Exiling a stolen permanent returns it under its owner's control")
    void stolenPermanentReturnsToOwner() {
        harness.addToBattlefield(player2, new NipGwyllion());
        UUID targetId = harness.getPermanentId(player2, "Nip Gwyllion");

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));

        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Nip Gwyllion"));

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        var target = harness.addToBattlefieldAndReturn(player2, new NipGwyllion());
        harness.setHand(player1, List.of(new Flickerwisp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = target.getId();
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
