package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireUrchin.class, Shock.class, Divination.class, GrizzlyBears.class})
class FireUrchinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant gives Fire Urchin +1/+0 until end of turn")
    void instantBoostsFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery gives Fire Urchin +1/+0 until end of turn")
    void sorceryBoostsFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature does not boost Fire Urchin")
    void creatureDoesNotBoostFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
    }

    @Test
    @DisplayName("The cast trigger boosts Fire Urchin before the instant resolves")
    void boostUsesTheStackAndResolvesBeforeTheSpell() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fireUrchin)).isEqualTo(3);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's instant does not boost Fire Urchin")
    void opponentInstantDoesNotBoostFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each cast boosts each controlled Fire Urchin and all boosts expire")
    void repeatedCastsBoostEachControlledUrchin() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new FireUrchin());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(1);
        harness.assertLife(player2, 16);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast boosts increase the excess combat damage dealt through trample")
    void boostedUrchinTramplesOverABlocker() {
        Permanent fireUrchin = addCreatureReady(player1, new FireUrchin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            resolveAllTriggers();
            assertThat(gd.stack).isEmpty();
        }

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(3);
        harness.assertLife(player2, 16);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fire Urchin");
    }
}
