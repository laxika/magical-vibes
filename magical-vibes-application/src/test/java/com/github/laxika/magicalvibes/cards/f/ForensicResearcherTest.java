package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForensicResearcher.class, GrizzlyBears.class, Island.class})
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

    @Test
    void cannotUntapItself() {
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, researcher.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(researcher.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapsALandWithoutCollectingEvidence() {
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(researcher.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void cannotUntapAnOpponentsPermanent() {
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(researcher.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotPayEvidenceWithInsufficientManaValue() {
        Card evidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence));
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForensicResearcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(researcher.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysExactEvidenceBeforeTheAbilityResolvesAndLeavesUnselectedCards() {
        Card evidence = new ForensicResearcher();
        Card remaining = new Island();
        harness.setGraveyard(player1, List.of(evidence, remaining));
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForensicResearcher());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));

        assertThat(researcher.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(evidence);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTapAnOpponentsNoncreaturePermanent() {
        harness.setGraveyard(player1, List.of(new ForensicResearcher()));
        Permanent researcher = addCreatureReady(player1, new ForensicResearcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(researcher.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }
}
