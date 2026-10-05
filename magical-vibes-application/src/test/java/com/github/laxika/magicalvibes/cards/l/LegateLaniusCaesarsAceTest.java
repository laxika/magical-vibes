package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BrotherhoodVertibird;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegateLaniusCaesarsAce.class, GrizzlyBears.class, BrotherhoodVertibird.class})
class LegateLaniusCaesarsAceTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a tenth of their creatures, rounded up")
    void eachOpponentSacrificesTenthRoundedUp() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        for (int i = 0; i < 11; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        Permanent legate = castLegate();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> chosen = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .limit(2)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, chosen);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(9);
        assertThat(legate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A single opponent creature is sacrificed without a choice")
    void singleOpponentCreatureIsAutomaticallySacrificed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent legate = castLegate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(legate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noOpponentCreaturesMeansNoSacrificeOrCounters() {
        harness.addToBattlefield(player2, new BrotherhoodVertibird());

        Permanent legate = castLegate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(legate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exactlyTenCreaturesRequiresOnlyOneSacrifice() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        harness.addToBattlefield(player2, new BrotherhoodVertibird());
        Permanent legate = castLegate();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        Permanent chosen = findPermanent(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(9);
        harness.assertOnBattlefield(player2, "Brotherhood Vertibird");
        assertThat(legate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingCrewedVehicleTriggersCounterUsingItsBattlefieldType() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new BrotherhoodVertibird());
        harness.addToBattlefield(player2, new LegateLaniusCaesarsAce());
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        Permanent legate = castLegate();
        harness.handleMultiplePermanentsChosen(player2, List.of(vehicle.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Brotherhood Vertibird");
        assertThat(legate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castLegate() {
        harness.setHand(player1, List.of(new LegateLaniusCaesarsAce()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Legate Lanius, Caesar's Ace");
    }
}
