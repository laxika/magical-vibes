package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.d.DralnusCrusade;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        NaturalEmergence.class,
        ForsakenCity.class,
        MeteorCrater.class,
        DralnusCrusade.class,
        CloudCover.class,
        AlphaKavu.class
})
class NaturalEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control become 2/2 creatures with first strike")
    void animatesOnlyControlledLands() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new ForsakenCity());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.addToBattlefield(player1, new NaturalEmergence());

        assertThat(gqs.isCreature(gd, ownLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownLand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.isLand(gd, ownLand)).isTrue();

        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLand, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.isLand(gd, opponentLand)).isTrue();
    }

    @Test
    @DisplayName("Lands entering after Natural Emergence also become 2/2 creatures with first strike")
    void animatesLandsThatEnterLater() {
        harness.addToBattlefield(player1, new NaturalEmergence());
        Permanent land = harness.enterBattlefieldAndReturn(player1, new ForsakenCity());

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animated lands stop being creatures when Natural Emergence leaves")
    void animationEndsWhenNaturalEmergenceLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForsakenCity());
        Permanent emergence = harness.addToBattlefieldAndReturn(player1, new NaturalEmergence());

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FIRST_STRIKE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, emergence));

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Enters with a non-targeting choice to return a controlled red or green enchantment")
    void etbChoosesMatchingEnchantment() {
        Permanent ownEligible = harness.addToBattlefieldAndReturn(player1, new DralnusCrusade());
        harness.addToBattlefield(player1, new CloudCover());
        harness.addToBattlefield(player1, new AlphaKavu());
        Permanent opponentEligible = harness.addToBattlefieldAndReturn(player2, new DralnusCrusade());

        harness.castFromHand(player1, new NaturalEmergence(), "{2}{R}{G}");
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID emergenceId = harness.getPermanentId(player1, "Natural Emergence");
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ownEligible.getId(), emergenceId);
        assertThat(choice.validIds()).doesNotContain(opponentEligible.getId());

        harness.handlePermanentChosen(player1, ownEligible.getId());

        harness.assertInHand(player1, "Dralnu's Crusade");
        harness.assertOnBattlefield(player1, "Natural Emergence");
        harness.assertOnBattlefield(player1, "Cloud Cover");
        harness.assertOnBattlefield(player1, "Alpha Kavu");
        harness.assertOnBattlefield(player2, "Dralnu's Crusade");
    }

    @Test
    @DisplayName("Its enters ability can return Natural Emergence itself")
    void etbCanReturnItself() {
        harness.addToBattlefield(player1, new DralnusCrusade());

        harness.castFromHand(player1, new NaturalEmergence(), "{2}{R}{G}");
        resolveAllTriggers();

        UUID emergenceId = harness.getPermanentId(player1, "Natural Emergence");
        harness.handlePermanentChosen(player1, emergenceId);

        harness.assertInHand(player1, "Natural Emergence");
        harness.assertOnBattlefield(player1, "Dralnu's Crusade");
        harness.assertNotOnBattlefield(player1, "Natural Emergence");
    }
}
