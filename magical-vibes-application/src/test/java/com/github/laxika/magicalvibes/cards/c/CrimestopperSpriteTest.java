package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({CrimestopperSprite.class, NervousGardener.class, Island.class})
class CrimestopperSpriteTest extends BaseCardTest {

    @Test
    void entersAndTapsTargetCreatureWithoutEvidence() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void collectingEvidenceAlsoPutsAStunCounterOnTargetCreature() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void mayDeclineEvidenceEvenWhenEnoughIsAvailable() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NervousGardener());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void alreadyTappedCreatureStillReceivesStunCounterAndSkipsOneUntap() {
        harness.setGraveyard(player1, List.of(new NervousGardener(), new NervousGardener(), new NervousGardener()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        target.setTapped(true);
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void evidenceMayExceedSixAndUnselectedCardsRemainInGraveyard() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener(), new Island());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence.get(4));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence.subList(0, 4));
    }

    @Test
    void insufficientEvidenceIsRejectedWithoutExilingCards() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("collect evidence 6");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Crimestopper Sprite");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetALand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new CrimestopperSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
