package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VituGhaziInspector.class, NervousGardener.class})
class VituGhaziInspectorTest extends BaseCardTest {

    @Test
    void entersWithoutEvidenceAndDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VituGhaziInspector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void collectingEvidencePutsCounterOnTargetCreatureAndGainsLife() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VituGhaziInspector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void canTargetItselfAfterCollectingMoreThanSixEvidence() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VituGhaziInspector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
        harness.passBothPriorities();
        Permanent inspector = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, inspector.getId());
        harness.passBothPriorities();

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
    }

    @Test
    void cannotCollectEvidenceBelowSixManaValue() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new VituGhaziInspector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetPreventsLifeGain() {
        harness.setGraveyard(player1, List.of(new NervousGardener(),
                new NervousGardener(), new NervousGardener()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VituGhaziInspector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
