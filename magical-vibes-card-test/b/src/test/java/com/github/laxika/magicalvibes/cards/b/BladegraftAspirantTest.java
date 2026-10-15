package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GoldwardensHelm;
import com.github.laxika.magicalvibes.cards.h.HexgoldHalberd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladegraftAspirant.class, BarbedBatterfist.class, CrawlingChorus.class,
        GoldwardensHelm.class, HexgoldHalberd.class})
class BladegraftAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Equipment spells cost {1} less to cast")
    void equipmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.castFromHand(player1, new BarbedBatterfist(), "{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Equipment abilities targeting Bladegraft Aspirant cost {1} less")
    void equipmentAbilitiesTargetingThisCreatureCostOneLess() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new BladegraftAspirant());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new BarbedBatterfist());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                aspirant.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(aspirant.getId());
    }

    @Test
    @DisplayName("Equipment abilities targeting another creature are not reduced")
    void equipmentAbilitiesTargetingAnotherCreatureAreNotReduced() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new BarbedBatterfist());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellReductionDoesNotRemoveColoredManaEvenWithMultipleAspirants() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentAspirantDoesNotReduceEquipmentSpellCost() {
        harness.addToBattlefield(player2, new BladegraftAspirant());
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonEquipmentSpellCostIsNotReduced() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.setHand(player1, List.of(new BladegraftAspirant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipReductionPreservesColoredMana() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new BladegraftAspirant());
        harness.addToBattlefield(player1, new HexgoldHalberd());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, aspirant.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, aspirant.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hexgold Halberd").getAttachedTo()).isEqualTo(aspirant.getId());
    }

    @Test
    void anotherAspirantDoesNotFurtherReduceEquipCostForThisCreature() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new BladegraftAspirant());
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.addToBattlefield(player1, new HexgoldHalberd());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, aspirant.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, aspirant.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hexgold Halberd").getAttachedTo()).isEqualTo(aspirant.getId());
    }

    @Test
    void spellReductionsFromMultipleAspirantsStack() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.addToBattlefield(player1, new BladegraftAspirant());

        harness.castFromHand(player1, new GoldwardensHelm(), "{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new BladegraftAspirant());
        addCreatureReady(player2, new CrawlingChorus());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BladegraftAspirant());
        Permanent first = addCreatureReady(player2, new BladegraftAspirant());
        Permanent second = addCreatureReady(player2, new BladegraftAspirant());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
