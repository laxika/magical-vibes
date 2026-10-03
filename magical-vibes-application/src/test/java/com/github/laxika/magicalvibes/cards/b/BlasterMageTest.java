package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.cards.w.WallOfDistortion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlasterMage.class, WallOfDistortion.class, Vendetta.class})
class BlasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}, Discard: destroys target Wall")
    void destroysTargetWall() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        prepareActivation();

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Wall of Distortion");
        harness.assertInGraveyard(player2, "Wall of Distortion");
        harness.assertInGraveyard(player1, "Blaster Mage");
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        addCreatureReady(player1, new BlasterMage());
        Permanent creature = addCreatureReady(player2, new BlasterMage());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareActivation() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new BlasterMage()));
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.setHand(player1, List.of(new BlasterMage()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        mage.tap();
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        mage.setSummoningSick(true);
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its controller's Wall and pays costs before resolution")
    void destroysOwnWallAndPaysCostsBeforeResolution() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player1, new WallOfDistortion());
        prepareActivation();

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Blaster Mage");
        harness.assertOnBattlefield(player1, "Wall of Distortion");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall of Distortion");
        harness.assertInGraveyard(player1, "Wall of Distortion");
    }

    @Test
    @DisplayName("Activated ability resolves after Blaster Mage is destroyed")
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.setHand(player1, List.of(new WallOfDistortion(), new Vendetta()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.castInstant(player1, 0, mage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blaster Mage");
        harness.assertInGraveyard(player1, "Blaster Mage");
        harness.assertOnBattlefield(player2, "Wall of Distortion");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Distortion");
        harness.assertInGraveyard(player2, "Wall of Distortion");
    }

    @Test
    @DisplayName("An ability whose Wall was destroyed in response does not refund its costs")
    void targetDestroyedInResponseDoesNotRefundCosts() {
        Permanent firstMage = addCreatureReady(player1, new BlasterMage());
        Permanent secondMage = addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.setHand(player1, List.of(new BlasterMage(), new BlasterMage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 1, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Distortion");
        harness.assertInGraveyard(player2, "Wall of Distortion");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(firstMage.isTapped()).isTrue();
        assertThat(secondMage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
