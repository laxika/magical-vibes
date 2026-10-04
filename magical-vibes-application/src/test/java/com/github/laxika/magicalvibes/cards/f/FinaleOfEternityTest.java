package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskmantleOperative;
import com.github.laxika.magicalvibes.cards.r.RagingKronch;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinaleOfEternity.class, DuskmantleOperative.class, RagingKronch.class, Plains.class})
class FinaleOfEternityTest extends BaseCardTest {

    @Test
    void xZeroWithNoTargetsResolvesWithoutReturningCreatures() {
        harness.setGraveyard(player1, List.of(new DuskmantleOperative()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Finale of Eternity");
        harness.assertInGraveyard(player1, "Duskmantle Operative");
        harness.assertNotOnBattlefield(player1, "Duskmantle Operative");
    }

    @Test
    void xAboveTenAlsoReturnsCreatures() {
        harness.setGraveyard(player1, List.of(new DuskmantleOperative()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 13);

        harness.castSorcery(player1, 0, 11);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Duskmantle Operative");
        harness.assertNotInGraveyard(player1, "Duskmantle Operative");
    }

    @Test
    void rejectsNoncreaturePermanent() {
        var land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 10, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    void destroysThreeTargetsUsingIndividualRatherThanCombinedToughness() {
        var first = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        var second = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        var third = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        var unselected = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unselected);
        harness.assertNotOnBattlefield(player1, "Raging Kronch");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Raging Kronch");
    }

    @Test
    void xNineDoesNotReturnCreatures() {
        harness.setGraveyard(player1, List.of(new DuskmantleOperative()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 11);

        harness.castSorcery(player1, 0, 9);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Duskmantle Operative");
        harness.assertNotOnBattlefield(player1, "Duskmantle Operative");
        harness.assertInGraveyard(player1, "Finale of Eternity");
    }

    @Test
    void xTenReturnsOwnCreatureDestroyedByTheSpell() {
        var own = harness.addToBattlefieldAndReturn(player1, new DuskmantleOperative());
        var opposing = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        harness.setGraveyard(player1, List.of(new RagingKronch()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castSorcery(player1, 0, 10, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Duskmantle Operative");
        harness.assertOnBattlefield(player1, "Raging Kronch");
        harness.assertNotInGraveyard(player1, "Duskmantle Operative");
        harness.assertInGraveyard(player2, "Raging Kronch");
        harness.assertNotOnBattlefield(player2, "Raging Kronch");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
    }

    @Test
    void allTargetsBecomingTooToughPreventsTheReturnEffect() {
        var target = harness.addToBattlefieldAndReturn(player2, new DuskmantleOperative());
        harness.setGraveyard(player1, List.of(new RagingKronch()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castSorcery(player1, 0, 10, List.of(target.getId()));
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Duskmantle Operative");
        harness.assertInGraveyard(player1, "Raging Kronch");
        harness.assertNotOnBattlefield(player1, "Raging Kronch");
        harness.assertInGraveyard(player1, "Finale of Eternity");
    }

    @Test
    void oneIllegalTargetDoesNotPreventDestroyingTheOtherOrReturningCreatures() {
        var illegal = harness.addToBattlefieldAndReturn(player2, new DuskmantleOperative());
        var legal = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        harness.setGraveyard(player1, List.of(new DuskmantleOperative()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castSorcery(player1, 0, 10, List.of(illegal.getId(), legal.getId()));
        illegal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Duskmantle Operative");
        harness.assertInGraveyard(player2, "Raging Kronch");
        harness.assertNotOnBattlefield(player2, "Raging Kronch");
        harness.assertOnBattlefield(player1, "Duskmantle Operative");
    }

    @Test
    void destroysUpToThreeCreaturesWithToughnessAtMostX() {
        var bears = harness.addToBattlefieldAndReturn(player2, new DuskmantleOperative());
        harness.addToBattlefield(player2, new RagingKronch());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Duskmantle Operative");
        harness.assertOnBattlefield(player2, "Raging Kronch");
    }

    @Test
    void rejectsCreatureWithToughnessGreaterThanX() {
        var giant = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness X or less");
    }

    @Test
    void withXTenReturnsAllCreatureCardsFromControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new DuskmantleOperative(), new RagingKronch(), new Plains()));
        harness.setGraveyard(player2, List.of(new DuskmantleOperative()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 13);

        harness.castSorcery(player1, 0, 10);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Duskmantle Operative")).hasSize(1);
        assertThat(findPermanents(player1, "Raging Kronch")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Finale of Eternity");
        harness.assertNotOnBattlefield(player2, "Duskmantle Operative");
    }
}
