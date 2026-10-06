package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvercladFerocidons.class, GrizzlyBears.class, Mountain.class, Shock.class})
class SilvercladFerocidonsTest extends BaseCardTest {

    @Test
    void damageTriggersOpponentToSacrificePermanent() {
        harness.addToBattlefield(player2, new SilvercladFerocidons());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOpponentPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ferocidonsId = harness.getPermanentId(player2, "Silverclad Ferocidons");
        harness.castAndResolveInstant(player1, 0, ferocidonsId);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(opponentPermanent.getId(), otherOpponentPermanent.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opponentPermanent.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Silverclad Ferocidons");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void controllerDealingDamageStillMakesOpponentSacrificeLand() {
        Permanent ferocidons = harness.addToBattlefieldAndReturn(player1, new SilvercladFerocidons());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ferocidons.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), mountain.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Silverclad Ferocidons");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentWithNoPermanentsDoesNotSacrificeControllersPermanents() {
        Permanent ferocidons = harness.addToBattlefieldAndReturn(player1, new SilvercladFerocidons());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ferocidons.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silverclad Ferocidons");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void separateDamageEventsEachTriggerIncludingLethalDamage() {
        Permanent ferocidons = harness.addToBattlefieldAndReturn(player1, new SilvercladFerocidons());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, ferocidons.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        harness.castAndResolveInstant(player1, 0, ferocidons.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.castAndResolveInstant(player1, 0, ferocidons.getId());
        harness.assertInGraveyard(player1, "Silverclad Ferocidons");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void simultaneousCombatDamageFromTwoSourcesTriggersOnlyOnce() {
        Permanent ferocidons = harness.addToBattlefieldAndReturn(player1, new SilvercladFerocidons());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        ferocidons.setAttacking(true);
        ferocidons.setSummoningSick(false);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 4, secondBlocker.getId(), 4));

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Silverclad Ferocidons");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
