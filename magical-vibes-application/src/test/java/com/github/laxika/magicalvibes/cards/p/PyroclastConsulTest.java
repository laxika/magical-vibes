package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KinsbaileCavalier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyroclastConsul.class, KinsbaileCavalier.class})
class PyroclastConsulTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the shared-type card deals 2 damage to each creature on both sides")
    void revealDealsDamageToEachCreature() {
        Permanent consul = addCreatureReady(player1, new PyroclastConsul());
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player2, new KinsbaileCavalier());
        PyroclastConsul topCard = new PyroclastConsul(); // Elemental Shaman — shares a type
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // 2/2 Kinsbaile Cavaliers die on both sides; the 3/3 Consul survives with 2 damage.
        harness.assertNotOnBattlefield(player1, "Kinsbaile Cavalier");
        harness.assertNotOnBattlefield(player2, "Kinsbaile Cavalier");
        harness.assertOnBattlefield(player1, "Pyroclast Consul");
        assertThat(consul.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining to reveal deals no damage")
    void decliningDealsNoDamage() {
        addCreatureReady(player1, new PyroclastConsul());
        Permanent creature = addCreatureReady(player2, new KinsbaileCavalier());
        PyroclastConsul topCard = new PyroclastConsul();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Kinsbaile Cavalier");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        Permanent consul = addCreatureReady(player1, new PyroclastConsul());
        harness.setLibrary(player1, List.of(new KinsbaileCavalier())); // Kithkin Knight — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(consul.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The kinship trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        Permanent consul = addCreatureReady(player1, new PyroclastConsul());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(consul.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kinship still deals damage when the Consul leaves before resolution")
    void kinshipResolvesAfterSourceLeavesBattlefield() {
        Permanent consul = addCreatureReady(player1, new PyroclastConsul());
        harness.addToBattlefield(player2, new KinsbaileCavalier());
        PyroclastConsul topCard = new PyroclastConsul();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, consul);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Pyroclast Consul");
        harness.assertNotOnBattlefield(player2, "Kinsbaile Cavalier");
        harness.assertInGraveyard(player2, "Kinsbaile Cavalier");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Kinship does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent consul = addCreatureReady(player1, new PyroclastConsul());
        harness.setLibrary(player1, List.of(new PyroclastConsul()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(consul.getMarkedDamage()).isZero();
    }
}
