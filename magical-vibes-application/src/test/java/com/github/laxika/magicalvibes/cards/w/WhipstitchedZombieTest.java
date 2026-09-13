package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WhipstitchedZombie.class)
class WhipstitchedZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {B} sacrifices Whipstitched Zombie")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new WhipstitchedZombie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Whipstitched Zombie");
        harness.assertInGraveyard(player1, "Whipstitched Zombie");
    }

    @Test
    @DisplayName("Paying {B} keeps Whipstitched Zombie on the battlefield")
    void payKeeps() {
        harness.addToBattlefield(player1, new WhipstitchedZombie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Accepting without black mana still sacrifices Whipstitched Zombie")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new WhipstitchedZombie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Whipstitched Zombie");
    }

    @Test
    @DisplayName("An upkeep trigger does not sacrifice a new Whipstitched Zombie after the original leaves")
    void triggerDoesNotAffectNewPermanent() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new WhipstitchedZombie());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));
        harness.addToBattlefield(player1, new WhipstitchedZombie());
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
        harness.assertInGraveyard(player1, "Whipstitched Zombie");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new WhipstitchedZombie());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
    }
}
