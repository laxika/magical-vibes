package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IllusionaryTerrainTest extends BaseCardTest {

    private Permanent terrainWithTypes(CardSubtype from, CardSubtype to) {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());
        terrain.setChosenSubtype(from);
        terrain.setSecondChosenSubtype(to);
        return terrain;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving Illusionary Terrain awaits two basic land type choices")
    void resolvingTriggersTwoBasicLandTypeChoices() {
        harness.setHand(player1, List.of(new IllusionaryTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "MOUNTAIN");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ISLAND");

        Permanent terrain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Illusionary Terrain"))
                .findFirst().orElseThrow();
        assertThat(terrain.getChosenSubtype()).isEqualTo(CardSubtype.MOUNTAIN);
        assertThat(terrain.getSecondChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("A basic Mountain taps for blue when Mountain→Island is chosen")
    void basicMountainProducesChosenSecondTypeMana() {
        harness.addToBattlefield(player1, new Mountain());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A basic Plains is unaffected when Mountain→Island is chosen")
    void otherBasicTypeUnaffected() {
        harness.addToBattlefield(player1, new Plains());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Also converts a basic Mountain the opponent controls")
    void convertsOpponentBasicMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).getFirst();
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.PLAINS);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, mountain);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("A nonbasic land is unaffected even if it would otherwise match")
    void nonbasicUnaffected() {
        harness.addToBattlefield(player1, new GhostQuarter());
        Permanent ghostQuarter = gd.playerBattlefields.get(player1.getId()).getFirst();
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, ghostQuarter);

        assertThat(bonus.landSubtypeOverriding()).isFalse();
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("A Mountain taps for red again once Illusionary Terrain leaves")
    void redResumesWhenTerrainLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent terrain = terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).remove(terrain);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Terrain")
    void paysCumulativeUpkeep() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(terrain.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(terrain);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Terrain")
    void declineSacrifices() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(terrain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusionary Terrain"));
    }
}
