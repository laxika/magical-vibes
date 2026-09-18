package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DistortingLens;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HiredGiant;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnaturalHunger.class, DistortingLens.class, FreshVolunteers.class, HiredGiant.class,
        JhovallRider.class})
class UnnaturalHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the enchanted creature's power at its controller's upkeep")
    void dealsEnchantedCreaturePowerDamage() {
        attachToOpponentCreature(new HiredGiant());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Uses the enchanted creature's last known power if it leaves before resolution")
    void usesLastKnownEnchantedCreaturePower() {
        Permanent enchanted = attachToOpponentCreature(new HiredGiant());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player2.getId()).remove(enchanted);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Sacrifices another creature instead of dealing damage")
    void sacrificesAnotherCreature() {
        attachToOpponentCreature(new HiredGiant());
        Permanent volunteers = addCreatureReady(player2, new FreshVolunteers());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(volunteers);
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player2, "Hired Giant");
    }

    @Test
    @DisplayName("Excludes the enchanted creature from the sacrifice choices")
    void excludesEnchantedCreatureFromSacrificeChoices() {
        Permanent enchanted = attachToOpponentCreature(new HiredGiant());
        Permanent volunteers = addCreatureReady(player2, new FreshVolunteers());
        Permanent rider = addCreatureReady(player2, new JhovallRider());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(volunteers.getId(), rider.getId());
        assertThat(choice.validIds()).doesNotContain(enchanted.getId());
    }

    @Test
    @DisplayName("Sacrifices the creature chosen from multiple available choices")
    void sacrificesChosenCreatureFromMultipleChoices() {
        Permanent enchanted = attachToOpponentCreature(new HiredGiant());
        Permanent volunteers = addCreatureReady(player2, new FreshVolunteers());
        Permanent rider = addCreatureReady(player2, new JhovallRider());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, volunteers.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(volunteers);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rider, enchanted);
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Uses the enchanted creature's power when the trigger resolves")
    void usesEnchantedCreaturePowerAtResolution() {
        Permanent enchanted = attachToOpponentCreature(new HiredGiant());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        enchanted.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("A triggered ability still deals damage if the Aura leaves before resolution")
    void triggerStillDealsDamageAfterAuraLeaves() {
        attachToOpponentCreature(new HiredGiant());
        Permanent aura = findPermanent(player1, "Unnatural Hunger");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Does not sacrifice a creature controlled by the Aura's controller")
    void onlyEnchantedCreatureControllerCanSacrifice() {
        attachToOpponentCreature(new HiredGiant());
        Permanent auraControllerCreature = addCreatureReady(player1, new FreshVolunteers());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraControllerCreature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's upkeep")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        attachToOpponentCreature(new HiredGiant());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new HiredGiant());
        harness.addToBattlefield(player2, new DistortingLens());
        Permanent artifact = findPermanent(player2, "Distorting Lens");

        harness.setHand(player1, List.of(new UnnaturalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachToOpponentCreature(Card creatureCard) {
        Permanent creature = addCreatureReady(player2, creatureCard);
        harness.setHand(player1, List.of(new UnnaturalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }
}
