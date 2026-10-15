package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JazalGoldmane;
import com.github.laxika.magicalvibes.cards.p.PilgrimsEye;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrantsFamiliar.class, JazalGoldmane.class, PilgrimsEye.class})
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
        Permanent victim = addCreatureReady(player2, new PilgrimsEye());

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(5);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Lieutenant attack trigger deals 7 damage to a defending creature")
    void attackTriggerDealsSevenDamage() {
        addCommanderToBattlefield();
        addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new PilgrimsEye());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(7);
    }

    @Test
    @DisplayName("Attack trigger targets only creatures controlled by the defending player")
    void attackTriggerTargetsOnlyDefendingCreatures() {
        addCommanderToBattlefield();
        addCreatureReady(player1, new TyrantsFamiliar());
        Permanent ownCreature = addCreatureReady(player1, new PilgrimsEye());
        Permanent defendingCreature = addCreatureReady(player2, new PilgrimsEye());

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    void controllingOpponentsCommanderDoesNotEnableLieutenant() {
        JazalGoldmane commander = new JazalGoldmane();
        gd.playerCommanders.put(player2.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new PilgrimsEye());

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(5);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    void attackTriggerStillDealsDamageAfterCommanderLeaves() {
        Permanent commander = addCommanderToBattlefield();
        addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new PilgrimsEye());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        declareAttackers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerCommandZones.get(player1.getId()).add(commander.getCard());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(7);
    }

    @Test
    void lieutenantBonusStopsImmediatelyWhenCommanderLeaves() {
        Permanent commander = addCommanderToBattlefield();
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(7);

        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerCommandZones.get(player1.getId()).add(commander.getCard());

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(5);
    }

    @Test
    void commanderInCommandZoneDoesNotEnableLieutenant() {
        JazalGoldmane commander = new JazalGoldmane();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent familiar = addCreatureReady(player1, new TyrantsFamiliar());
        addCreatureReady(player2, new PilgrimsEye());

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(5);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lieutenantAttackDamageDestroysCreatureWithLethalDamage() {
        addCommanderToBattlefield();
        addCreatureReady(player1, new TyrantsFamiliar());
        Permanent victim = addCreatureReady(player2, new PilgrimsEye());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pilgrim's Eye");
        harness.assertInGraveyard(player2, "Pilgrim's Eye");
    }

    private Permanent addCommanderToBattlefield() {
        JazalGoldmane commander = new JazalGoldmane();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        return addCreatureReady(player1, commander);
    }
}