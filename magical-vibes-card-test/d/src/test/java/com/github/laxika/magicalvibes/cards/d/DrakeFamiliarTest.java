package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakeFamiliar.class, DoublingSeason.class, PeelFromReality.class})
class DrakeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no enchantments")
    void autoSacrificesWithNoEnchantments() {
        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInGraveyard(player1, "Drake Familiar");
    }

    @Test
    @DisplayName("An opponent's enchantment can be returned to its owner's hand")
    void opponentEnchantmentCanBeReturned() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new DrakeFamiliar()).getId();
        harness.addToBattlefield(player2, new DoublingSeason());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID enchantmentId = findPermanent(player2, "Doubling Season").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(enchantmentId)
                .doesNotContain(creatureId);
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player2, "Doubling Season");
        harness.assertInHand(player2, "Doubling Season");
        harness.assertOnBattlefield(player2, "Drake Familiar");
    }

    @Test
    @DisplayName("Returning a controlled enchantment keeps Drake Familiar")
    void returningEnchantmentKeepsDrakeFamiliar() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID enchantmentId = findPermanent(player1, "Doubling Season").getId();
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player1, "Doubling Season");
        harness.assertInHand(player1, "Doubling Season");
    }

    @Test
    @DisplayName("Declining to return an enchantment sacrifices Drake Familiar")
    void decliningReturnSacrificesDrakeFamiliar() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castDrakeFamiliar();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInGraveyard(player1, "Drake Familiar");
        harness.assertOnBattlefield(player1, "Doubling Season");
    }

    @Test
    @DisplayName("An enchantment can still be returned after Drake Familiar leaves the battlefield")
    void canReturnEnchantmentAfterSourceLeaves() {
        UUID opponentDrakeId = harness.addToBattlefieldAndReturn(player2, new DrakeFamiliar()).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new DoublingSeason()).getId();
        harness.castFromHand(player1, new DrakeFamiliar(), "{1}{U}");
        harness.passBothPriorities();
        UUID drakeId = findPermanent(player1, "Drake Familiar").getId();

        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(drakeId, opponentDrakeId));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInHand(player1, "Drake Familiar");
        harness.assertNotInGraveyard(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player2, "Doubling Season");
        harness.assertInHand(player2, "Doubling Season");
    }

    @Test
    @DisplayName("Only the chosen enchantment is returned when both players have enchantments")
    void returnsOnlyChosenEnchantment() {
        UUID ownEnchantmentId = harness.addToBattlefieldAndReturn(player1, new DoublingSeason()).getId();
        UUID opponentEnchantmentId = harness.addToBattlefieldAndReturn(player2, new DoublingSeason()).getId();

        castDrakeFamiliar();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(ownEnchantmentId, opponentEnchantmentId);
        harness.handlePermanentChosen(player1, opponentEnchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertOnBattlefield(player1, "Doubling Season");
        harness.assertNotInHand(player1, "Doubling Season");
        harness.assertNotOnBattlefield(player2, "Doubling Season");
        harness.assertInHand(player2, "Doubling Season");
    }

    private void castDrakeFamiliar() {
        harness.castFromHand(player1, new DrakeFamiliar(), "{1}{U}");
        resolveAllTriggers();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Declining to return an enchantment asks for Drake Familiar's death replacement before it is sacrificed")
    void decliningAsksForTheDeathReplacementBeforeDrakeFamiliarIsSacrificed() {
        harness.castFromHand(player1, new DrakeFamiliar(), "{1}{U}");
        harness.passBothPriorities();
        Permanent drake = findPermanent(player1, "Drake Familiar");
        enchantWithMagemarkAgainstKalitas(drake);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertDeathReplacementAskedWhileDrakeFamiliarStays(drake);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertDrakeFamiliarReturnedToHandWithoutZombie(drake);
        harness.assertInGraveyard(player1, "Necromancer's Magemark");
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private void assertDeathReplacementAskedWhileDrakeFamiliarStays(Permanent creature) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
    }

    private void assertDrakeFamiliarReturnedToHandWithoutZombie(Permanent creature) {
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void enchantWithMagemarkAgainstKalitas(Permanent creature) {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(creature.getId());
    }
}
