package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.w.WearAway;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenjuOfTheFalls.class, Island.class, Forest.class, StoneRain.class, WearAway.class})
class GenjuOfTheFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Genju of the Falls cannot enchant a non-Island land")
    void cannotEnchantNonIsland() {
        harness.addToBattlefield(player1, new Island()); // legal target so the spell is castable
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new GenjuOfTheFalls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Island");
    }

    @Test
    @DisplayName("Activating {2} makes the enchanted Island a 3/2 blue flying Spirit that is still a land")
    void activationAnimatesEnchantedIsland() {
        Permanent island = addIslandWithGenju();

        activateGenju();

        assertThat(gqs.isCreature(gd, island)).isTrue();
        assertThat(island.getEffectivePower()).isEqualTo(3);
        assertThat(island.getEffectiveToughness()).isEqualTo(2);
        assertThat(island.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(island.getAnimatedColor()).isEqualTo(CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, island, Keyword.FLYING)).isTrue();
        assertThat(gqs.isLand(gd, island)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent island = addIslandWithGenju();
        activateGenju();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, island)).isFalse();
        assertThat(gqs.hasKeyword(gd, island, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Destroying the enchanted Island lets you return Genju from your graveyard to your hand")
    void returnsFromGraveyardWhenIslandDies() {
        Permanent island = addIslandWithGenju();

        destroyIsland(island);
        harness.passBothPriorities(); // resolve the "may return" trigger
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Falls");
        harness.assertNotInGraveyard(player1, "Genju of the Falls");
    }

    @Test
    @DisplayName("Declining the trigger leaves Genju in the graveyard")
    void decliningLeavesGenjuInGraveyard() {
        Permanent island = addIslandWithGenju();

        destroyIsland(island);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Falls");
    }

    @Test
    @DisplayName("The trigger returns only the Genju that enchanted the destroyed Island")
    void returnsOnlyTheTriggeringGenju() {
        Permanent island = addIslandWithGenju();
        UUID attachedGenjuId = findPermanent(player1, "Genju of the Falls").getCard().getId();
        GenjuOfTheFalls otherGenju = new GenjuOfTheFalls();
        harness.setGraveyard(player1, List.of(otherGenju));

        destroyIsland(island);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(attachedGenjuId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(otherGenju)
                .noneMatch(card -> card.getId().equals(attachedGenjuId));
    }

    @Test
    @DisplayName("Removing Genju in response does not stop its animation ability")
    void animationResolvesAfterAuraIsDestroyed() {
        Permanent island = addIslandWithGenju();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Falls"));
        harness.activateAbility(player1, genjuIndex, null, null);

        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Genju of the Falls"));
        harness.assertInGraveyard(player1, "Genju of the Falls");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, island)).isTrue();
        assertThat(island.getEffectivePower()).isEqualTo(3);
        assertThat(island.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, island, Keyword.FLYING)).isTrue();
        assertThat(gqs.isLand(gd, island)).isTrue();
    }

    @Test
    @DisplayName("Destroying Genju after animation does not end the animation or return the Aura")
    void animationPersistsAfterAuraIsDestroyed() {
        Permanent island = addIslandWithGenju();
        activateGenju();

        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Genju of the Falls"));

        assertThat(gqs.isCreature(gd, island)).isTrue();
        assertThat(island.getEffectivePower()).isEqualTo(3);
        assertThat(island.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, island, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Genju of the Falls");
        harness.assertNotInHand(player1, "Genju of the Falls");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Genju can enchant an opponent's Island and returns to the Aura controller's hand")
    void canEnchantOpponentsIslandAndReturnToOwnHand() {
        harness.addToBattlefield(player2, new Island());
        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new GenjuOfTheFalls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, islandId);
        harness.passBothPriorities();

        activateGenju();
        Permanent island = findPermanent(player2, "Island");
        assertThat(gqs.isCreature(gd, island)).isTrue();
        assertThat(island.getEffectivePower()).isEqualTo(3);

        destroyIsland(island);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Falls");
        harness.assertNotInHand(player2, "Genju of the Falls");
        harness.assertInGraveyard(player2, "Island");
    }

    private Permanent addIslandWithGenju() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.setHand(player1, List.of(new GenjuOfTheFalls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, islandId);
        harness.passBothPriorities();

        return findPermanent(player1, "Island");
    }

    private void activateGenju() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Falls"));
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
    }

    private void destroyIsland(Permanent island) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, island.getId());
    }
}
