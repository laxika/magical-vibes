package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DunbarrowRevivalist.class, GrizzlyBears.class, DarksteelRelic.class})
class DunbarrowRevivalistTest extends BaseCardTest {

    @Test
    void createsOneWickedRoleForTheNextControlledCreature() {
        harness.enterBattlefieldAndReturn(player1, new DunbarrowRevivalist());
        harness.passBothPriorities();

        Permanent firstCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    void bargainedRevivalistReturnsACreatureCardFromTheGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new DunbarrowRevivalist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
