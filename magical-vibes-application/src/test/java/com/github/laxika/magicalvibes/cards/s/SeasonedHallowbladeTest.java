package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasonedHallowblade.class, Plains.class, Island.class, Shock.class, SwiftResponse.class})
class SeasonedHallowbladeTest extends BaseCardTest {

    @Test
    void activationRequiresDiscardingACard() {
        addBladeReady(player1);
        harness.setHand(player1, List.of(new Plains(), new Island()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);
    }

    @Test
    void resolvingAbilityDiscardsCardTapsBladeAndGrantsIndestructible() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(blade.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    void indestructibleResetsAtEndOfTurn() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addBladeReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void discardIsPaidBeforeTapAndIndestructibleResolve() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(blade.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(blade.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canActivateWhileTappedAndAgainWhileIndestructible() {
        Permanent blade = addBladeReady(player1);
        blade.setTapped(true);
        harness.setHand(player1, List.of(new Island(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(blade.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canActivateWithSummoningSickness() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SeasonedHallowblade());
        blade.setSummoningSick(true);
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(blade.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void activationInResponseToLethalDamageProtectsBlade() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, blade.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seasoned Hallowblade");
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void indestructibleProtectsAgainstDestroyEffects() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Island()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SwiftResponse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, blade.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seasoned Hallowblade");
        harness.assertInGraveyard(player2, "Swift Response");
    }

    private Permanent addBladeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SeasonedHallowblade());
        perm.setSummoningSick(false);
        return perm;
    }
}
