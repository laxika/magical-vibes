package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InfernalTribute;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RogueElephant.class, Forest.class, InfernalTribute.class, WindingCanyons.class})
class RogueElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no Forest")
    void autoSacrificesWithoutForest() {
        harness.addToBattlefield(player1, new WindingCanyons());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player1, "Winding Canyons");
    }

    @Test
    @DisplayName("An opponent's Forest doesn't satisfy the requirement")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player2, new Forest());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing a Forest keeps Rogue Elephant on the battlefield")
    void sacrificingForestKeepsElephant() {
        harness.addToBattlefield(player1, new Forest());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID forestId = findPermanent(player1, "Forest").getId();
        harness.handlePermanentChosen(player1, forestId);

        harness.assertOnBattlefield(player1, "Rogue Elephant");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining to sacrifice a Forest sacrifices Rogue Elephant")
    void decliningSacrificesElephant() {
        harness.addToBattlefield(player1, new Forest());

        castElephant();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Only Forests are offered as the permanent to sacrifice")
    void onlyForestsAreValidChoices() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new WindingCanyons());

        castElephant();

        harness.handleMayAbilityChosen(player1, true);

        UUID windingCanyonsId = findPermanent(player1, "Winding Canyons").getId();
        UUID forestId = findPermanent(player1, "Forest").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(forestId)
                .doesNotContain(windingCanyonsId);
    }

    @Test
    @DisplayName("May still sacrifice a Forest after the Elephant leaves before its ability resolves")
    void maySacrificeForestAfterElephantLeavesBattlefield() {
        harness.addToBattlefield(player1, new InfernalTribute());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new WindingCanyons()));

        castElephantSpell();

        UUID elephantId = findPermanent(player1, "Rogue Elephant").getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elephantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID forestId = findPermanent(player1, "Forest").getId();
        harness.handlePermanentChosen(player1, forestId);

        harness.assertOnBattlefield(player1, "Infernal Tribute");
        harness.assertNotOnBattlefield(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Forest");
    }

    private void castElephant() {
        castElephantSpell();
        harness.passBothPriorities(); // resolve ETB
    }

    private void castElephantSpell() {
        harness.castFromHand(player1, new RogueElephant(), "{G}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
    }
}
