package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemandingDragon.class, GreenwoodSentinel.class})
class DemandingDragonTest extends BaseCardTest {

    private void castAndResolveToChoice(boolean opponentHasCreature) {
        if (opponentHasCreature) {
            harness.addToBattlefield(player2, new GreenwoodSentinel());
        }

        harness.setHand(player1, List.of(new DemandingDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent with no creatures takes 5 damage")
    void noCreatureTakesDamage() {
        castAndResolveToChoice(false);

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Demanding Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent may sacrifice a creature to prevent the damage")
    void sacrificesCreatureInsteadOfTakingDamage() {
        castAndResolveToChoice(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demanding Dragon");
    }

    @Test
    @DisplayName("Opponent may decline and take 5 damage")
    void declinesSacrificeAndTakesDamage() {
        castAndResolveToChoice(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demanding Dragon");
    }

    @Test
    @DisplayName("Opponent chooses which of multiple creatures to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        castAndResolveToChoice(false);

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(chosen);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demanding Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller's creatures cannot satisfy the opponent's sacrifice")
    void controllerCreaturesCannotPreventDamage() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        castAndResolveToChoice(false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demanding Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
