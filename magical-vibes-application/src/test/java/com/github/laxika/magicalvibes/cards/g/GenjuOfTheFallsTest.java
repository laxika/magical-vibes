package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(island.getCard().getType()).isEqualTo(CardType.LAND);
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

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Genju of the Falls"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Genju of the Falls"));
    }

    @Test
    @DisplayName("Declining the trigger leaves Genju in the graveyard")
    void decliningLeavesGenjuInGraveyard() {
        Permanent island = addIslandWithGenju();

        destroyIsland(island);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Genju of the Falls"));
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
        harness.castSorcery(player2, 0, island.getId());
        harness.passBothPriorities(); // resolve Stone Rain
    }
}
