package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CampaignOfVengeance.class, FieldCreeper.class})
class CampaignOfVengeanceTest extends BaseCardTest {

    @Test
    void triggersWhenCreatureYouControlAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new CampaignOfVengeance());
        addCreatureReady(player1, new FieldCreeper());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void triggersOnceForEachAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new CampaignOfVengeance());
        addCreatureReady(player1, new FieldCreeper());
        addCreatureReady(player1, new FieldCreeper());

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotTriggerForOpponentCreatureAttacks() {
        harness.addToBattlefield(player1, new CampaignOfVengeance());
        addCreatureReady(player2, new FieldCreeper());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterCampaignAndAttackerLeaveBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new CampaignOfVengeance());
        Permanent attacker = addCreatureReady(player1, new FieldCreeper());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(campaign.getCard());
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void multipleCampaignsEachTriggerForTheSameAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CampaignOfVengeance());
        harness.addToBattlefield(player1, new CampaignOfVengeance());
        addCreatureReady(player1, new FieldCreeper());

        declareAttackers(List.of(2));
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}

