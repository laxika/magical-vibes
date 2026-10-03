package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DunbarrowRevivalist.class, GrizzlyBears.class, DarksteelRelic.class, RaiseTheAlarm.class})
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

    @Test
    void unbargainedEntryDoesNotReturnACreatureCard() {
        harness.setGraveyard(player1, List.of(new DunbarrowRevivalist()));

        harness.castFromHand(player1, new DunbarrowRevivalist(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dunbarrow Revivalist");
        harness.assertNotInHand(player1, "Dunbarrow Revivalist");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opposingCreatureDoesNotConsumeTheBoon() {
        harness.enterBattlefieldAndReturn(player1, new DunbarrowRevivalist());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Wicked");

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void boonSurvivesItsSourceAndWickedRoleDrainsOnlyTheOpponent() {
        Permanent revivalist = harness.enterBattlefieldAndReturn(player1, new DunbarrowRevivalist());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, revivalist));

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, role));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void boonStillAttachesWhenTheEnteringCreatureChangesControllerBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new DunbarrowRevivalist());
        harness.passBothPriorities();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    void simultaneousEntrantsAllowChoosingEitherCreatureForTheRole() {
        harness.enterBattlefieldAndReturn(player1, new DunbarrowRevivalist());
        harness.passBothPriorities();
        harness.castFromHand(player1, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .toList();
        assertThat(soldiers).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                soldiers.get(0).getId(), soldiers.get(1).getId());
        harness.handlePermanentChosen(player1, soldiers.get(1).getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(soldiers.get(1).getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Wicked"))).hasSize(1);
    }
}
