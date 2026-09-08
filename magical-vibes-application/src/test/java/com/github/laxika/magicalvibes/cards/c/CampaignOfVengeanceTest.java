package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampaignOfVengeanceTest extends BaseCardTest {

    @Test
    void triggersWhenCreatureYouControlAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCampaignReady(player1);
        addCreatureReady(player1);

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

        addCampaignReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotTriggerForOpponentCreatureAttacks() {
        addCampaignReady(player1);
        addCreatureReady(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCampaignReady(Player player) {
        Permanent permanent = new Permanent(new CampaignOfVengeance());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setColor(CardColor.WHITE);
        creature.setPower(2);
        creature.setToughness(2);

        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
