package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.u.UlnaAlleyShopkeep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DigSiteInventory.class, UlnaAlleyShopkeep.class})
class DigSiteInventoryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter and grants vigilance to target creature you control")
    void putsCounterAndGrantsVigilance() {
        Permanent creature = addCreatureReady(player1, new UlnaAlleyShopkeep());
        harness.setHand(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        Permanent creature = addCreatureReady(player1, new UlnaAlleyShopkeep());
        harness.setGraveyard(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Dig Site Inventory");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dig Site Inventory"));
    }

    @Test
    @DisplayName("Vigilance expires at end of turn while the counter remains")
    void vigilanceExpiresButCounterRemains() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent creature = addCreatureReady(player1, new UlnaAlleyShopkeep());
        harness.setHand(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast normally and then flashed back for another counter")
    void normalCastThenFlashbackAppliesBothEffectsAgain() {
        Permanent creature = addCreatureReady(player1, new UlnaAlleyShopkeep());
        harness.setHand(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Dig Site Inventory");

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dig Site Inventory");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dig Site Inventory"));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature from hand")
    void cannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new UlnaAlleyShopkeep());
        harness.setHand(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Flashback cannot target an opponent's creature")
    void flashbackCannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new UlnaAlleyShopkeep());
        harness.setGraveyard(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Flashback exiles without applying either effect if control of the target changes")
    void flashbackDoesNotResolveWhenTargetChangesController() {
        Permanent creature = addCreatureReady(player1, new UlnaAlleyShopkeep());
        harness.setGraveyard(player1, List.of(new DigSiteInventory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFlashback(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        harness.assertNotInGraveyard(player1, "Dig Site Inventory");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dig Site Inventory"));
    }
}
