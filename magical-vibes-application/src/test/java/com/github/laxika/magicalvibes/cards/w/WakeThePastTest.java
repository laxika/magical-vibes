package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GenesisChamber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeThePast.class, ArcboundWorker.class, DarksteelRelic.class, GrizzlyBears.class,
        GenesisChamber.class})
class WakeThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all own artifact cards and gives returned creatures haste")
    void returnsOwnArtifactsWithHaste() {
        Card worker = new ArcboundWorker();
        Card relic = new DarksteelRelic();
        Card ownCreature = new GrizzlyBears();
        Card opponentArtifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(worker, relic, ownCreature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(worker, relic);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature)
                .anyMatch(card -> card instanceof WakeThePast);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
        Permanent returnedWorker = findPermanent(player1, "Arcbound Worker");
        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new ArcboundWorker()));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent returnedWorker = findPermanent(player1, "Arcbound Worker");
        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Returning artifacts enter simultaneously and see each other's entry")
    void returnedChamberSeesSimultaneouslyReturnedCreature() {
        harness.setGraveyard(player1, List.of(new ArcboundWorker(), new GenesisChamber()));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        Permanent myr = findPermanent(player1, "Myr");
        assertThat(myr.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Resolves with no artifacts in the graveyard")
    void resolvesWithoutArtifacts() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature)
                .anyMatch(card -> card instanceof WakeThePast);
        assertThat(gd.stack).isEmpty();
    }

    private void addWakeThePastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
