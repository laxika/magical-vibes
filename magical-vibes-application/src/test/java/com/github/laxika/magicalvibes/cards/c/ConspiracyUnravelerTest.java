package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FurtiveCourier;
import com.github.laxika.magicalvibes.cards.d.Doppelgang;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConspiracyUnraveler.class, GrizzlyBears.class, FurtiveCourier.class,
        CrimestopperSprite.class, Doppelgang.class})
class ConspiracyUnravelerTest extends BaseCardTest {

    @Test
    void mayCollectEvidenceInsteadOfPayingManaForCreatureSpells() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        Card thirdEvidence = new GrizzlyBears();
        Card fourthEvidence = new GrizzlyBears();
        Card fifthEvidence = new GrizzlyBears();
        List<Card> evidence = List.of(firstEvidence, secondEvidence, thirdEvidence,
                fourthEvidence, fifthEvidence);

        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        harness.setGraveyard(player1, evidence);
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));

        assertThat(gd.stack.getLast().isAlternateCost()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void canPayManaWithoutCollectingEvidence() {
        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        Card evidence = new ConspiracyUnraveler();
        harness.setGraveyard(player1, List.of(evidence));
        harness.setHand(player1, List.of(new FurtiveCourier()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Furtive Courier");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotCollectEvidenceBelowTen() {
        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        Card evidence = new ConspiracyUnraveler();
        harness.setGraveyard(player1, List.of(evidence));
        harness.setHand(player1, List.of(new FurtiveCourier()));

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseAlternativeCostAfterSourceLosesAbilities() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ConspiracyUnraveler());
        source.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setGraveyard(player1, List.of(new ConspiracyUnraveler(), new FurtiveCourier()));
        harness.setHand(player1, List.of(new FurtiveCourier()));

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void alternativeEvidenceDoesNotPaySpritesOptionalAdditionalCost() {
        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurtiveCourier());
        List<Card> evidence = List.of(new ConspiracyUnraveler(), new FurtiveCourier());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new CrimestopperSprite()));

        gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void cannotChooseNonzeroXWithEvidenceAlternativeCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ConspiracyUnraveler());
        harness.setGraveyard(player1, List.of(new ConspiracyUnraveler(), new FurtiveCourier()));
        harness.setHand(player1, List.of(new Doppelgang()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, source.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
