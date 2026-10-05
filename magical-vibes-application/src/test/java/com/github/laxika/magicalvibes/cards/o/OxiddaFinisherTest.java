package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GoldwardensHelm;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OxiddaFinisher.class, GoldwardensHelm.class, PropheticPrism.class, CrawlingChorus.class})
class OxiddaFinisherTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Equipment reduces the generic mana cost")
    void affinityForEquipmentReducesGenericCost() {
        harness.addToBattlefield(player1, new GoldwardensHelm());
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Equipment controlled by the spell's controller")
    void affinityCountsOnlyControlledEquipment() {
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player2, new GoldwardensHelm());
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleEquipmentEachReduceTheCost() {
        harness.addToBattlefield(player1, new GoldwardensHelm());
        harness.addToBattlefield(player1, new GoldwardensHelm());
        harness.addToBattlefield(player1, new GoldwardensHelm());
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oxidda Finisher");
    }

    @Test
    void excessEquipmentCannotReduceColoredMana() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GoldwardensHelm());
        }
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void attachedTappedEquipmentStillReducesTheCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldwardensHelm());
        equipment.setAttachedTo(creature.getId());
        equipment.setTapped(true);
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void equipmentOutsideTheBattlefieldDoesNotReduceTheCost() {
        harness.setHand(player1, List.of(new OxiddaFinisher(), new GoldwardensHelm()));
        harness.setGraveyard(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OxiddaFinisher());
        Permanent blocker = addCreatureReady(player2, new CrawlingChorus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Crawling Chorus");
        harness.assertOnBattlefield(player1, "Oxidda Finisher");
    }
}
