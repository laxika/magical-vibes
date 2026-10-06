package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlashThompsonSpiderFan;
import com.github.laxika.magicalvibes.cards.f.FlyingOctobot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScoutTheCity.class, FlyingOctobot.class, Forest.class, FlashThompsonSpiderFan.class, Shock.class})
class ScoutTheCityTest extends BaseCardTest {

    @Test
    @DisplayName("Look Around mills three, returns a milled permanent, and gains life")
    void lookAroundReturnsMilledPermanentAndGainsLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new Shock()));
        harness.setLife(player1, 20);

        castLookAround();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Look Around gains life when no milled permanent is available")
    void lookAroundGainsLifeWithoutPermanent() {
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLife(player1, 20);

        castLookAround();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Bring Down destroys a creature with flying")
    void bringDownDestroysCreatureWithFlying() {
        Permanent target = addCreatureReady(player2, new FlyingOctobot());
        castBringDown(target);

        harness.assertNotOnBattlefield(player2, "Flying Octobot");
        harness.assertInGraveyard(player2, "Flying Octobot");
    }

    @Test
    @DisplayName("Bring Down cannot target a creature without flying")
    void bringDownRejectsCreatureWithoutFlying() {
        Permanent target = addCreatureReady(player2, new FlashThompsonSpiderFan());
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Look Around may leave every milled permanent in the graveyard")
    void lookAroundCanDeclineReturn() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new Shock()));
        harness.setLife(player1, 20);

        castLookAround();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Look Around can choose a later permanent but returns only one")
    void lookAroundReturnsOnlyOneOfMultiplePermanents() {
        Forest first = new Forest();
        FlyingOctobot second = new FlyingOctobot();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLife(player1, 20);

        castLookAround();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third);
        harness.assertLife(player1, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Look Around only offers permanents milled by this spell")
    void lookAroundDoesNotReturnEarlierGraveyardCards() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLife(player1, 20);

        castLookAround();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Look Around mills a short library and still returns a permanent and gains life")
    void lookAroundWithShortLibrary() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        castLookAround();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Look Around gains life with an empty library")
    void lookAroundWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        castLookAround();

        harness.assertLife(player1, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bring Down can destroy its controller's flying creature without applying Look Around")
    void bringDownCanTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new FlyingOctobot());
        Shock libraryCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);

        castBringDown(target);

        harness.assertNotOnBattlefield(player1, "Flying Octobot");
        harness.assertInGraveyard(player1, "Flying Octobot");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Bring Down does not destroy a target that loses flying before resolution")
    void bringDownRechecksFlyingOnResolution() {
        Permanent target = addCreatureReady(player2, new FlyingOctobot());
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));

        target.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Flying Octobot");
        harness.assertNotInGraveyard(player2, "Flying Octobot");
        harness.assertInGraveyard(player1, "Scout the City");
    }

    @Test
    @DisplayName("Bring Down cannot target a noncreature permanent")
    void bringDownRejectsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    private void castLookAround() {
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void castBringDown(Permanent target) {
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
