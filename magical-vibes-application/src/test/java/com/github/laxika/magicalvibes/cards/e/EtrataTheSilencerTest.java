package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtrataTheSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles a chosen defending creature with a hit counter and shuffles Etrata")
    void exilesChosenDefendingCreatureAndShufflesSource() {
        Permanent etrata = addAttackingEtrata();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveEtrataTrigger(opposingCreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingCreature.getCard());
        assertThat(gd.exiledCardHitCounters).containsEntry(opposingCreature.getCard().getId(), 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(etrata);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(etrata.getCard());
    }

    @Test
    @DisplayName("The combat trigger only offers creatures controlled by the damaged player")
    void targetsOnlyDamagedPlayersCreatures() {
        addAttackingEtrata();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingCreature.getId()).doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("The damaged player loses after their third owned exiled hit-counter card")
    void thirdHitCounterCardMakesDamagedPlayerLose() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        gd.addToExile(player2.getId(), first);
        gd.addToExile(player2.getId(), second);
        gd.exiledCardHitCounters.put(first.getId(), 1);
        gd.exiledCardHitCounters.put(second.getId(), 1);

        Permanent etrata = addAttackingEtrata();
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveEtrataTrigger(opposingCreature);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.exiledCardHitCounters).containsEntry(opposingCreature.getCard().getId(), 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(etrata.getCard());
    }

    private Permanent addAttackingEtrata() {
        Permanent etrata = addCreatureReady(player1, new EtrataTheSilencer());
        etrata.setAttacking(true);
        return etrata;
    }

    private void resolveEtrataTrigger(Permanent target) {
        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
