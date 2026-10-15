package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningGeysermage.class, GrizzlyBears.class, FountainOfYouth.class})
class CunningGeysermageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new CunningGeysermage(), "{2}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
    }

    @Test
    void kickedReturnsTheTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
    }

    @Test
    void kickedMayChooseNoTarget() {
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cunning Geysermage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedCannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void kickedCanReturnAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
    }

    @Test
    void kickedMayDeclineEvenWhenAnotherCreatureIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedTriggerCannotTargetTheGeysermageItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent geysermage = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CunningGeysermage)
                .findFirst().orElseThrow();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(geysermage.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, geysermage.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cunning Geysermage");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
