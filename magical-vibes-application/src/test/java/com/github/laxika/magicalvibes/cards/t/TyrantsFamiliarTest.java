package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrantsFamiliar.class, EdgarMarkov.class, GrizzlyBears.class})
class TyrantsFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant gives Tyrant's Familiar +2/+2")
    void lieutenantBoostsFamiliar() {
        addCommanderToBattlefield();
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(7);
    }

    @Test
    @DisplayName("Without a commander, lieutenant abilities do not apply")
    void noCommanderMeansNoLieutenantAbilities() {
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        familiar.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(5);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Lieutenant attack trigger deals 7 damage to a defending creature")
    void attackTriggerDealsSevenDamage() {
        addCommanderToBattlefield();
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(7);
    }

    @Test
    @DisplayName("Attack trigger targets only creatures controlled by the defending player")
    void attackTriggerTargetsOnlyDefendingCreatures() {
        addCommanderToBattlefield();
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    private void addCommanderToBattlefield() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
    }
}
