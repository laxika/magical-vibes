package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DormantGomazoa.class, Shock.class, TurnToFrog.class})
class DormantGomazoaTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new DormantGomazoa()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dormant Gomazoa").isTapped()).isTrue();
    }

    @Test
    void doesNotUntapDuringControllerUntapStep() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        harness.performUntapStep(player1);

        assertThat(gomazoa.isTapped()).isTrue();
    }

    @Test
    void controllerBecomingTargetOfOpponentSpellMayLeaveItTapped() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gomazoa.isTapped()).isTrue();
    }

    @Test
    void controllerBecomingTargetOfOwnSpellMayUntapIt() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gomazoa.isTapped()).isFalse();
    }

    @Test
    void targetingCreatureDoesNotTriggerUntap() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, gomazoa.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gomazoa.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Dormant Gomazoa");
    }

    @Test
    void targetingOtherPlayerDoesNotTriggerUntap() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gomazoa.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotTriggerAfterLosingAbilities() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, gomazoa.getId());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gomazoa.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }
}
