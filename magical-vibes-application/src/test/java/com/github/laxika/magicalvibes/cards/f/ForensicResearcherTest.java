package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForensicResearcher.class, GrizzlyBears.class})
class ForensicResearcherTest extends BaseCardTest {

    @Test
    void untapsAnotherPermanentYouControl() {
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(researcher.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void collectsEvidenceToTapAnOpposingCreature() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(researcher.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void cannotTargetACreatureYouControl() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
