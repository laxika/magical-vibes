package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ClayFiredBricks.class, CosmiumKiln.class, DarksteelRelic.class, LlanowarElves.class,
        Plains.class, EnsoulArtifact.class})
class ClayFiredBricksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by finding a basic Plains and gaining two life")
    void entersWithPlainsAndLife() {
        harness.setLibrary(player1, List.of(new Plains(), new LlanowarElves()));
        harness.castFromHand(player1, new ClayFiredBricks(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns transformed with two Gnomes")
    void craftsFromBattlefieldArtifact() {
        Permanent bricks = harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bricks, relic);
        assertThat(gd.findExiledCard(relic.getCard().getId())).isNotNull();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent kiln = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CosmiumKiln)
                .findFirst().orElseThrow();
        assertThat(kiln.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Gnome")).hasSize(2);
        List<Permanent> gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gnome"))
                .toList();
        assertThat(gnomes).allMatch(gnome -> gqs.getEffectivePower(gd, gnome) == 2);
    }

    @Test
    @DisplayName("Craft can exile an artifact card from the graveyard")
    void craftsFromGraveyardArtifact() {
        harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        DarksteelRelic relic = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(relic));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.findExiledCard(relic.getId())).isNotNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof CosmiumKiln);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof ClayFiredBricks);
    }

    @Test
    @DisplayName("Craft prompts when more than one material is available")
    void choosesCraftMaterial() {
        harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        DarksteelRelic graveyardRelic = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(graveyardRelic));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CraftMaterialChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardRelic.getId()));
        assertThat(gd.findExiledCard(graveyardRelic.getId())).isNotNull();
    }

    @Test
    @DisplayName("Failing to find a Plains still gains two life")
    void gainsLifeAfterFailingToFind() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.castFromHand(player1, new ClayFiredBricks(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An empty library does not prevent gaining two life")
    void gainsLifeWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new ClayFiredBricks(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Craft cannot use its source, nonartifacts, or an opponent's artifacts")
    void cannotCraftWithoutEligibleMaterial() {
        Permanent bricks = harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        harness.addToBattlefield(player1, new Plains());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.addToBattlefield(player2, new ClayFiredBricks());
        harness.setGraveyard(player2, List.of(new ClayFiredBricks()));
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bricks);
        assertThat(gd.findExiledCard(bricks.getCard().getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Craft cannot be activated while a spell is on the stack")
    void craftRequiresEmptyStack() {
        Permanent bricks = harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        harness.setGraveyard(player1, List.of(new ClayFiredBricks()));
        harness.castFromHand(player1, new ClayFiredBricks(), "{1}{W}");
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bricks);
        assertThat(gd.findExiledCard(bricks.getCard().getId())).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cosmium Kiln boosts existing and newly entered friendly creatures only")
    void kilnBoostsOnlyFriendlyCreatures() {
        harness.addToBattlefield(player1, new ClayFiredBricks());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new ClayFiredBricks()));
        addCraftMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, friendly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, friendly)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cosmium Kiln also boosts itself when it becomes a creature")
    void animatedKilnReceivesItsOwnBonus() {
        harness.addToBattlefield(player1, new ClayFiredBricks());
        harness.setGraveyard(player1, List.of(new ClayFiredBricks()));
        addCraftMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent kiln = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CosmiumKiln)
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, kiln.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, kiln)).isTrue();
        assertThat(gqs.getEffectivePower(gd, kiln)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, kiln)).isEqualTo(6);
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
