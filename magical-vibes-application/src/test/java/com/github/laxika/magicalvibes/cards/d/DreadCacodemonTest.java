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

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, target.getName());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Dread Cacodemon").isTapped()).isFalse();
    }
}
