package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaplingNursery.class, Forest.class, Mountain.class})
class SaplingNurseryTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less to cast for each Forest you control")
    void costsLessForEachForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Landfall creates a 3/4 green Treefolk token with reach")
    void landfallCreatesTreefolk() {
        harness.addToBattlefield(player1, new SaplingNursery());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent treefolk = findPermanent(player1, "Treefolk");
        assertThat(treefolk.getEffectivePower()).isEqualTo(3);
        assertThat(treefolk.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Exiling Sapling Nursery grants indestructible to Treefolk and Forests until end of turn")
    void activationGrantsIndestructibleToTreefolkAndForests() {
        Permanent nursery = harness.addToBattlefieldAndReturn(player1, new SaplingNursery());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent treefolk = findPermanent(player1, "Treefolk");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nursery);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A Forest controlled by an opponent does not reduce the cost")
    void opponentForestDoesNotReduceCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Affinity cannot reduce the two green mana symbols")
    void affinityDoesNotReduceColoredMana() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A non-Forest land also triggers landfall")
    void mountainTriggersLandfall() {
        harness.addToBattlefield(player1, new SaplingNursery());
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treefolk")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new SaplingNursery());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treefolk")).isZero();
    }

    @Test
    @DisplayName("Exile is paid immediately and protection affects only your permanents at resolution")
    void exileCostAndProtectionSnapshot() {
        SaplingNursery card = new SaplingNursery();
        Permanent nursery = harness.addToBattlefieldAndReturn(player1, card);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nursery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        Permanent newForest = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, newForest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(countPermanents(player1, "Treefolk")).isZero();
    }

    @Test
    @DisplayName("A pending landfall trigger still resolves after Nursery is exiled")
    void pendingLandfallResolvesAfterExile() {
        harness.addToBattlefield(player1, new SaplingNursery());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treefolk = findPermanent(player1, "Treefolk");
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(countPermanents(player1, "Treefolk")).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands without the Forest subtype do not reduce the casting cost")
    void mountainDoesNotReduceCost() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
