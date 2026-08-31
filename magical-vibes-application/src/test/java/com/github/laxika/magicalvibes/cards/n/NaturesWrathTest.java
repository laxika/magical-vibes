package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturesWrath.class, AirElemental.class, BogWraith.class, FugitiveWizard.class,
        GrizzlyBears.class, Island.class, Swamp.class})
class NaturesWrathTest extends BaseCardTest {

    private void putWrathOnBattlefield(Player controller) {
        harness.addToBattlefield(controller, new NaturesWrath());
    }

    private PendingInteraction.MultiPermanentChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("A blue creature entering makes its controller sacrifice a blue permanent")
    void blueCreatureEnteringTriggersSacrifice() {
        putWrathOnBattlefield(player2);
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities(); // resolve the creature
        harness.passBothPriorities(); // resolve Nature's Wrath's trigger

        // The Wizard is the only blue permanent player1 controls, so it is sacrificed with no choice.
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("With several eligible permanents the entering permanent's controller chooses one")
    void controllerChoosesWhichBluePermanentToSacrifice() {
        putWrathOnBattlefield(player2);
        harness.addToBattlefield(player1, new AirElemental());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).hasSize(2);

        Permanent elemental = findPermanents(player1, "Air Elemental").getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(elemental.getId()));

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("A black creature entering makes its controller sacrifice a black permanent")
    void blackCreatureEnteringTriggersSacrifice() {
        putWrathOnBattlefield(player2);
        harness.castFromHand(player1, new BogWraith(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bog Wraith");
    }

    @Test
    @DisplayName("An Island entering triggers even though a basic Island is colorless")
    void islandEnteringTriggersSacrifice() {
        putWrathOnBattlefield(player2);
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("A Swamp entering triggers even though a basic Swamp is colorless")
    void swampEnteringTriggersSacrifice() {
        putWrathOnBattlefield(player2);
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("A green creature entering does not trigger either ability")
    void greenCreatureDoesNotTrigger() {
        putWrathOnBattlefield(player2);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(activeChoice()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to pay {G} at upkeep sacrifices Nature's Wrath")
    void decliningUpkeepPaymentSacrificesEnchantment() {
        putWrathOnBattlefield(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nature's Wrath");
    }

    @Test
    @DisplayName("Paying {G} at upkeep keeps Nature's Wrath on the battlefield")
    void payingUpkeepKeepsEnchantment() {
        putWrathOnBattlefield(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Nature's Wrath");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
