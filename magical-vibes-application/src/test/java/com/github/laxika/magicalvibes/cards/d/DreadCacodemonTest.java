package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadCacodemon.class, BeaconOfUnrest.class, GrizzlyBears.class})
class DreadCacodemonTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, destroys opponents' creatures and taps other creatures you control")
    void castFromHandDestroysOpponentsCreaturesAndTapsOtherCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DreadCacodemon()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Dread Cacodemon").isTapped()).isFalse();
    }

    @Test
    @DisplayName("When it enters from a graveyard, its hand-cast ability does not trigger")
    void enteringFromGraveyardDoesNotTriggerAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        DreadCacodemon target = new DreadCacodemon();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, target.getName());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Dread Cacodemon").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps other creatures even when opponents control no creatures")
    void tapsOtherCreaturesWithNoOpposingCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreadCacodemon()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Dread Cacodemon").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Reanimation neither destroys opposing creatures nor taps your other creatures")
    void reanimationDoesNotApplyEitherEffect() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DreadCacodemon target = new DreadCacodemon();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dread Cacodemon");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps another Dread Cacodemon and destroys every opposing creature")
    void onlyEnteringCacodemonIsExcludedFromTapping() {
        Permanent otherCacodemon = harness.addToBattlefieldAndReturn(player1, new DreadCacodemon());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DreadCacodemon enteringCard = new DreadCacodemon();
        harness.setHand(player1, List.of(enteringCard));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstOpponent.getCard(), secondOpponent.getCard());
        assertThat(otherCacodemon.isTapped()).isTrue();
        Permanent enteringPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(enteringCard.getId()))
                .findFirst().orElseThrow();
        assertThat(enteringPermanent.isTapped()).isFalse();
    }
}
