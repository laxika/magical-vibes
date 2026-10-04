package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtravagantReplication.class, GrizzlyBears.class, Forest.class})
class ExtravagantReplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep presents a target for another nonland permanent you control")
    void upkeepPresentsTargetSelection() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving the upkeep trigger creates a token copy of the target")
    void createsTokenCopy() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getName().equals("Grizzly Bears")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("The trigger has no legal target when only lands or opponents' permanents remain")
    void rejectsInvalidTargets() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent replication = harness.addToBattlefieldAndReturn(player1, new ExtravagantReplication());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(replication);
        gd.playerGraveyards.get(player1.getId()).add(replication.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void doesNotCopyTargetThatLeavesBattlefield() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyTargetNowControlledByOpponent() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyDoesNotInheritCountersDamageOrTappedStatus() {
        harness.addToBattlefield(player1, new ExtravagantReplication());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        bears.tap();
        bears.setMarkedDamage(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getPlusOnePlusOneCounters()).isZero();
                    assertThat(token.getMarkedDamage()).isZero();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                });
    }
}
