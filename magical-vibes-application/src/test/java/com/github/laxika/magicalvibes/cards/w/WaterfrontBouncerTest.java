package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterfrontBouncer.class, Forest.class, FreshVolunteers.class})
class WaterfrontBouncerTest extends BaseCardTest {

    @Test
    void discardingACardReturnsTargetCreatureToItsOwnersHand() {
        Permanent bouncer = addCreatureReady(player1, new WaterfrontBouncer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInHand(player2, "Fresh Volunteers");
        assertThat(bouncer.isTapped()).isTrue();
    }

    @Test
    void abilityCannotTargetANoncreaturePermanent() {
        addCreatureReady(player1, new WaterfrontBouncer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotBeActivatedWithoutACardToDiscard() {
        addCreatureReady(player1, new WaterfrontBouncer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureToItsOwnersHandWhenControllerDiffers() {
        addCreatureReady(player1, new WaterfrontBouncer());
        FreshVolunteers targetCard = new FreshVolunteers();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fresh Volunteers");
        harness.assertNotInHand(player2, "Fresh Volunteers");
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
    }

    @Test
    void abilityFizzlesIfTargetCreatureLeavesBeforeResolution() {
        Permanent bouncer = addCreatureReady(player1, new WaterfrontBouncer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(bouncer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityCannotBeActivatedWhenBouncerIsTapped() {
        Permanent bouncer = addCreatureReady(player1, new WaterfrontBouncer());
        bouncer.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
