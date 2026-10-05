package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalGryff.class, GrizzlyBears.class, Island.class})
class LoyalGryffTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return another creature you control")
    void enteringMayReturnAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        castAndResolveSpell();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Loyal Gryff");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(islandId).isNotEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Declining the ETB ability returns no creature")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAndResolveSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Loyal Gryff");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new LoyalGryff(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting with no other creature does not return Loyal Gryff")
    void noOtherCreatureDoesNothing() {
        castAndResolveSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Loyal Gryff");
        harness.assertNotInHand(player1, "Loyal Gryff");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's creature cannot be chosen")
    void opponentsCreatureIsExcluded() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LoyalGryff());
        harness.addToBattlefield(player2, new LoyalGryff());
        castAndResolveSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        harness.assertInHand(player1, "Loyal Gryff");
        harness.assertOnBattlefield(player1, "Loyal Gryff");
        harness.assertOnBattlefield(player2, "Loyal Gryff");
        harness.assertNotInHand(player2, "Loyal Gryff");
    }

    @Test
    @DisplayName("A borrowed creature returns to its owner's hand")
    void borrowedCreatureReturnsToOwner() {
        LoyalGryff borrowedCard = new LoyalGryff();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowedCreature = harness.addToBattlefieldAndReturn(player1, borrowedCard);
        castAndResolveSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, borrowedCreature.getId());

        harness.assertInHand(player2, "Loyal Gryff");
        harness.assertNotInHand(player1, "Loyal Gryff");
        harness.assertOnBattlefield(player1, "Loyal Gryff");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(borrowedCreature.getId()));
    }
}
