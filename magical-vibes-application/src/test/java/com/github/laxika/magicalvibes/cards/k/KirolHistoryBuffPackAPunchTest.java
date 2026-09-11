package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KirolHistoryBuffPackAPunchTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when a card leaves its controller's graveyard")
    void becomesPreparedWhenCardLeavesOwnGraveyard() {
        Permanent kirol = addCreatureReady(player1, new KirolHistoryBuffPackAPunch());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();
        assertThat(kirol.isPrepared()).isFalse();

        harness.passBothPriorities();

        assertThat(kirol.isPrepared()).isTrue();
        assertThat(kirol.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(kirol.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger when a card enters its controller's graveyard")
    void doesNotTriggerWhenCardEntersOwnGraveyard() {
        Permanent kirol = addCreatureReady(player1, new KirolHistoryBuffPackAPunch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(kirol.isPrepared()).isFalse();
        assertThat(kirol.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Pack a Punch mills a card, adds two counters, and grants trample")
    void packAPunchMillsCountersAndGrantsTrample() {
        Permanent kirol = prepareKirol();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card milledCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(milledCard);
        var copyId = kirol.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        assertThat(kirol.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("Pack a Punch's trample grant wears off at end of turn")
    void packAPunchTrampleWearsOffAtEndOfTurn() {
        Permanent kirol = prepareKirol();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).addFirst(new Shock());
        var copyId = kirol.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent prepareKirol() {
        Permanent kirol = addCreatureReady(player1, new KirolHistoryBuffPackAPunch());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return kirol;
    }
}
