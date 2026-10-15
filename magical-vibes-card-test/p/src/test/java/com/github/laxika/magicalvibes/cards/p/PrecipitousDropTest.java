package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrecipitousDrop.class, HillGiantHerdgorger.class, NeverwinterDryad.class})
class PrecipitousDropTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature and makes its controller venture")
    void entersAndVentures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new PrecipitousDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives the enchanted creature -2/-2 before dungeon completion")
    void givesBasePenalty() {
        Permanent creature = addEnchantedCreature();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives the enchanted creature -5/-5 after dungeon completion")
    void givesCompletedDungeonPenalty() {
        Permanent creature = addEnchantedCreature();
        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void opponentsCompletedDungeonDoesNotIncreasePenalty() {
        Permanent creature = addEnchantedCreature();
        gd.playersWhoCompletedDungeon.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void enteringAuraCanCompleteDungeonAndIncreaseItsPenalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        harness.setLibrary(player1, List.of(new NeverwinterDryad()));
        harness.setHand(player1, List.of(new PrecipitousDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playersWhoCompletedDungeon).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void ventureStillResolvesAfterCreatureAndAuraGoToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        harness.setHand(player1, List.of(new PrecipitousDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Neverwinter Dryad");
        harness.assertInGraveyard(player1, "Precipitous Drop");
        harness.handleListChoice(player1, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PrecipitousDrop());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
