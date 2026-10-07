package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulsAttendant.class, GlorySeeker.class, Plains.class})
class SoulsAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 1 life when another creature enters")
    void mayGainLifeWhenAnotherCreatureEnters() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GlorySeeker(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when declining")
    void doesNotGainLifeWhenDeclining() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GlorySeeker(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when Soul's Attendant itself enters")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new SoulsAttendant(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its controller may gain life when an opponent's creature enters without being cast")
    void triggersForOpponentsCreatureEnteringWithoutBeingCast() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new GlorySeeker());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An existing Attendant triggers for a second Attendant entering")
    void existingAttendantTriggersForAnotherAttendant() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new SoulsAttendant(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Each Attendant offers an independent optional life gain")
    void eachAttendantOffersIndependentChoice() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GlorySeeker(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger for a noncreature permanent entering")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new Plains());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
