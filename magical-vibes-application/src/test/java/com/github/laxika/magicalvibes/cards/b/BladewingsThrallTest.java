package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladewingsThrall.class, DragonWhelp.class})
class BladewingsThrallTest extends BaseCardTest {

    @Test
    void hasFlyingWhileYouControlADragon() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new BladewingsThrall());
        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new DragonWhelp());

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isTrue();
    }

    @Test
    void opponentDragonDoesNotGrantFlying() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new BladewingsThrall());
        harness.addToBattlefield(player2, new DragonWhelp());

        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FLYING)).isFalse();
    }

    @Test
    void mayReturnFromGraveyardWhenAnyDragonEnters() {
        BladewingsThrall thrall = new BladewingsThrall();
        harness.setGraveyard(player1, List.of(thrall));

        harness.enterBattlefieldAndReturn(player2, new DragonWhelp());
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

        harness.enterBattlefieldAndReturn(player2, new DragonWhelp());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Bladewing's Thrall");
        harness.assertNotOnBattlefield(player1, "Bladewing's Thrall");
    }
}
