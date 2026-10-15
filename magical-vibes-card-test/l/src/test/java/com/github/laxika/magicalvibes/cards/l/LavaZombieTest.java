package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavaZombie.class, MaggotCarrier.class, MoggSentry.class, AlphaKavu.class})
class LavaZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts to return a black or red creature you control")
    void enteringPromptsForBlackOrRedCreature() {
        UUID maggotCarrierId = addCreatureReady(player1, new MaggotCarrier()).getId();
        UUID moggSentryId = addCreatureReady(player1, new MoggSentry()).getId();
        UUID alphaKavuId = addCreatureReady(player1, new AlphaKavu()).getId();

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        UUID lavaZombieId = harness.getPermanentId(player1, "Lava Zombie");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(maggotCarrierId, moggSentryId, lavaZombieId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(alphaKavuId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Entering does not offer a matching creature controlled by an opponent")
    void enteringOnlyOffersCreaturesControlledByItsController() {
        addCreatureReady(player1, new AlphaKavu());
        Permanent opponentCreature = addCreatureReady(player2, new MaggotCarrier());

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        UUID lavaZombieId = harness.getPermanentId(player1, "Lava Zombie");

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(lavaZombieId)
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("The chosen black or red creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        UUID maggotCarrierId = addCreatureReady(player1, new MaggotCarrier()).getId();
        addCreatureReady(player1, new AlphaKavu());

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, maggotCarrierId);

        harness.assertInHand(player1, "Maggot Carrier");
        harness.assertOnBattlefield(player1, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Lava Zombie");
    }

    @Test
    @DisplayName("The activated ability gives Lava Zombie +1/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent lavaZombie = addCreatureReady(player1, new LavaZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lavaZombie.getPowerModifier()).isEqualTo(1);
        assertThat(lavaZombie.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void activatedAbilityBoostExpires() {
        Permanent lavaZombie = addCreatureReady(player1, new LavaZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lavaZombie.getPowerModifier()).isZero();
        assertThat(lavaZombie.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Lava Zombie must return itself when it is the only eligible creature")
    void returnsItselfWhenNoOtherCreatureQualifies() {
        addCreatureReady(player1, new AlphaKavu());

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        UUID lavaZombieId = harness.getPermanentId(player1, "Lava Zombie");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(lavaZombieId);

        harness.handlePermanentChosen(player1, lavaZombieId);

        harness.assertInHand(player1, "Lava Zombie");
        harness.assertNotOnBattlefield(player1, "Lava Zombie");
        harness.assertOnBattlefield(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("A red creature can be returned instead of Lava Zombie")
    void returnsRedCreature() {
        UUID moggSentryId = addCreatureReady(player1, new MoggSentry()).getId();

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, moggSentryId);

        harness.assertInHand(player1, "Mogg Sentry");
        harness.assertNotOnBattlefield(player1, "Mogg Sentry");
        harness.assertOnBattlefield(player1, "Lava Zombie");
    }

    @Test
    @DisplayName("A creature controlled by you returns to its owner's hand")
    void returnsBorrowedCreatureToOwner() {
        MaggotCarrier carrier = new MaggotCarrier();
        carrier.setOwnerId(player2.getId());
        UUID carrierId = addCreatureReady(player1, carrier).getId();

        harness.castFromHand(player1, new LavaZombie(), "{1}{B}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, carrierId);

        harness.assertInHand(player2, "Maggot Carrier");
        harness.assertNotInHand(player1, "Maggot Carrier");
        harness.assertNotOnBattlefield(player1, "Maggot Carrier");
        harness.assertOnBattlefield(player1, "Lava Zombie");
    }

    @Test
    @DisplayName("Repeated activations stack and do not boost another creature")
    void repeatedActivationsBoostOnlySelf() {
        Permanent lavaZombie = addCreatureReady(player1, new LavaZombie());
        Permanent otherCreature = addCreatureReady(player1, new MaggotCarrier());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(lavaZombie.getPowerModifier()).isEqualTo(2);
        assertThat(lavaZombie.getToughnessModifier()).isZero();
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
    }
}
