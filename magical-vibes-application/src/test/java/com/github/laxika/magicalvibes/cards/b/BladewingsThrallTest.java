package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonTyrant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladewingsThrall.class, DragonTyrant.class})
class BladewingsThrallTest extends BaseCardTest {

    @Test
    void hasFlyingWhileYouControlADragon() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new BladewingsThrall());
        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new DragonTyrant());

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isTrue();
    }

    @Test
    void opponentDragonDoesNotGrantFlying() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new BladewingsThrall());
        harness.addToBattlefield(player2, new DragonTyrant());

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isFalse();
    }

    @Test
    void losesFlyingWhenYouNoLongerControlADragon() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new BladewingsThrall());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonTyrant());

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isFalse();
    }

    @Test
    void mayReturnFromGraveyardWhenAnyDragonEnters() {
        BladewingsThrall thrall = new BladewingsThrall();
        harness.setGraveyard(player1, List.of(thrall));

        harness.enterBattlefieldAndReturn(player2, new DragonTyrant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bladewing's Thrall");
        harness.assertNotInGraveyard(player1, "Bladewing's Thrall");
    }

    @Test
    void decliningReturnKeepsCardInGraveyard() {
        harness.setGraveyard(player1, List.of(new BladewingsThrall()));

        harness.enterBattlefieldAndReturn(player2, new DragonTyrant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Bladewing's Thrall");
        harness.assertNotOnBattlefield(player1, "Bladewing's Thrall");
    }

    @Test
    void nonDragonEnteringDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new BladewingsThrall()));

        harness.enterBattlefieldAndReturn(player2, new BladewingsThrall());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bladewing's Thrall");
        harness.assertNotOnBattlefield(player1, "Bladewing's Thrall");
    }

    @Test
    void returningForYourOwnDragonGrantsFlying() {
        harness.setGraveyard(player1, List.of(new BladewingsThrall()));

        harness.enterBattlefieldAndReturn(player1, new DragonTyrant());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bladewing's Thrall");
        harness.assertNotInGraveyard(player1, "Bladewing's Thrall");
        Permanent returnedThrall = findPermanent(player1, "Bladewing's Thrall");
        assertThat(gqs.hasKeyword(gd, returnedThrall, Keyword.FLYING)).isTrue();
    }

    @Test
    void returnsEvenIfDragonLeavesBeforeTriggerResolves() {
        harness.setGraveyard(player1, List.of(new BladewingsThrall()));
        Permanent dragon = harness.enterBattlefieldAndReturn(player1, new DragonTyrant());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bladewing's Thrall");
        harness.assertNotInGraveyard(player1, "Bladewing's Thrall");
        Permanent returnedThrall = findPermanent(player1, "Bladewing's Thrall");
        assertThat(gqs.hasKeyword(gd, returnedThrall, Keyword.FLYING)).isFalse();
    }

    @Test
    void returnDoesNotIncludeAnotherThrallPutInGraveyardAfterDragonEntered() {
        BladewingsThrall triggeringThrall = new BladewingsThrall();
        BladewingsThrall laterThrall = new BladewingsThrall();
        harness.setGraveyard(player1, List.of(triggeringThrall));
        harness.enterBattlefieldAndReturn(player2, new DragonTyrant());
        harness.setGraveyard(player1, List.of(triggeringThrall, laterThrall));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(triggeringThrall.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(laterThrall);
    }
}
