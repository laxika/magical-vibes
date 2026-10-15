package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideChimera.class, GloriousAnthem.class, GrizzlyBears.class})
class RiptideChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep choice includes only enchantments the controller controls")
    void choiceIncludesOnlyControlledEnchantments() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new RiptideChimera());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentAnthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(chimera.getId(), anthem.getId())
                .doesNotContain(creature.getId(), opponentAnthem.getId());
    }

    @Test
    @DisplayName("Chosen enchantment is returned to its owner's hand")
    void chosenEnchantmentIsReturnedToHand() {
        harness.addToBattlefieldAndReturn(player1, new RiptideChimera());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, anthem.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(anthem.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GloriousAnthem);
    }

    @Test
    @DisplayName("Can return itself when it is the only enchantment controlled")
    void canReturnItself() {
        RiptideChimera chimeraCard = new RiptideChimera();
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, chimeraCard);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(chimera.getId());
        harness.handlePermanentChosen(player1, chimera.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(chimera.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(chimeraCard);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new RiptideChimera());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Riptide Chimera");
    }

    @Test
    @DisplayName("Returns an enchantment to its owner rather than its controller")
    void returnsOpponentOwnedEnchantmentToOwner() {
        harness.addToBattlefield(player1, new RiptideChimera());
        GloriousAnthem anthemCard = new GloriousAnthem();
        anthemCard.setOwnerId(player2.getId());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, anthemCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, anthem.getId());

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerHands.get(player2.getId())).contains(anthemCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(anthemCard);
        harness.assertOnBattlefield(player1, "Riptide Chimera");
    }

    @Test
    @DisplayName("The trigger still returns an enchantment after its source leaves play")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new RiptideChimera());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(chimera);
        gd.playerHands.get(player1.getId()).add(chimera.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(anthem.getId());
        harness.handlePermanentChosen(player1, anthem.getId());

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInHand(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("The trigger does nothing when no enchantments remain at resolution")
    void noEnchantmentRemainingAtResolution() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new RiptideChimera());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(chimera);
        gd.playerHands.get(player1.getId()).add(chimera.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
