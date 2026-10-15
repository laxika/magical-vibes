package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fly.class, HillGiantHerdgorger.class})
class FlyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature's combat damage makes its controller venture")
    void combatDamageMakesCreatureControllerVenture() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        attachAura(player1, creature);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.setLibrary(player1, List.of());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("A creature controller ventures when another player controls Fly")
    void creatureControllerVentureIsNotAuraController() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        attachAura(player1, creature);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.setLibrary(player2, List.of());
        resolveAllTriggers();
        harness.handleListChoice(player2, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void resolvesAttachedToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new Fly()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fly").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void removingFlyRemovesBothGrantedAbilities() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        attachAura(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Fly"));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void twoCopiesGrantTwoSeparateVentureAbilities() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        attachAura(player1, creature);
        attachAura(player1, creature);
        harness.setLibrary(player1, List.of());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Goblin Bazaar");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 2));
    }

    private void attachAura(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new Fly());
        aura.setAttachedTo(creature.getId());
    }
}
