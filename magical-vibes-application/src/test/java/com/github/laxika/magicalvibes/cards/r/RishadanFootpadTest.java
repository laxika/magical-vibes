package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RishadanFootpad.class, FreshVolunteers.class})
class RishadanFootpadTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent may pay {2} to keep their permanents")
    void opponentMayPayToKeepPermanent() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        castRishadanFootpad();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent who declines sacrifices a permanent of their choice")
    void opponentDeclinesAndSacrificesPermanent() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        castRishadanFootpad();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("An opponent with no permanents may still pay {2}")
    void opponentWithNoPermanentsMayStillPay() {
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        castRishadanFootpad();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent chooses which permanent to sacrifice")
    void opponentChoosesWhichPermanentToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        castRishadanFootpad();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Generic payment can use colored mana")
    void opponentCanPayWithColoredMana() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        castRishadanFootpad();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        harness.assertOnBattlefield(player1, "Rishadan Footpad");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private void castRishadanFootpad() {
        harness.castFromHand(player1, new RishadanFootpad(), "{3}{U}");
    }
}
