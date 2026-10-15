package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VilespawnSpider.class, GrizzlyBears.class, LightningBolt.class})
class VilespawnSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger mills a card")
    void upkeepTriggerMillsACard() {
        harness.addToBattlefield(player1, new VilespawnSpider());
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    @DisplayName("Ability creates an Insect for each creature card in the graveyard, counting the sacrificed Spider")
    void abilityCreatesInsectPerCreatureCardInGraveyard() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vilespawn Spider");
        harness.assertInGraveyard(player1, "Vilespawn Spider");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot be activated outside a main phase")
    void abilityIsSorcerySpeedOnly() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotMillOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new VilespawnSpider());
        harness.setLibrary(player1, List.of(new VilespawnSpider()));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void countsGraveyardAtResolutionAndIgnoresOpponentsGraveyard() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.setGraveyard(player2, List.of(new VilespawnSpider(), new VilespawnSpider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Vilespawn Spider");
        harness.assertInGraveyard(player1, "Vilespawn Spider");
        assertThat(countPermanents(player1, "Insect")).isZero();
        harness.setGraveyard(player1, List.of(new VilespawnSpider(), new VilespawnSpider(), new VilespawnSpider()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(3);
        assertThat(countPermanents(player2, "Insect")).isZero();
        assertThat(findPermanents(player1, "Insect")).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        });
    }

    @Test
    void createsNoTokensIfGraveyardIsEmptyAtResolution() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new VilespawnSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Vilespawn Spider");
        harness.assertNotInGraveyard(player1, "Vilespawn Spider");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VilespawnSpider(), "{G}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Vilespawn Spider");
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new VilespawnSpider()).tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Vilespawn Spider");
    }
}
