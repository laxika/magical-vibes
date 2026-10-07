package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreefolkSeedlings.class, Forest.class, Plains.class})
class TreefolkSeedlingsTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals the number of Forests you control; power stays 2")
    void toughnessEqualsControlledForests() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only your Forests, not opponent Forests")
    void countsOnlyControllersForests() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(1);
    }

    @Test
    @DisplayName("Toughness updates when Forests change")
    void toughnessUpdatesWhenForestsChange() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dies with no controlled Forests even when the opponent controls Forests")
    void diesWithoutControlledForests() {
        addReady(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Treefolk Seedlings");
        harness.assertInGraveyard(player1, "Treefolk Seedlings");
    }

    @Test
    @DisplayName("Survives with one controlled Forest")
    void survivesWithOneForest() {
        addReady(player1);
        harness.addToBattlefield(player1, new Forest());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Treefolk Seedlings");
        harness.assertNotInGraveyard(player1, "Treefolk Seedlings");
    }

    @Test
    @DisplayName("Characteristic toughness works in hand and graveyard")
    void toughnessWorksOutsideBattlefield() {
        TreefolkSeedlings seedlings = new TreefolkSeedlings();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(seedlings));

        assertThat(gqs.getEffectiveCardToughness(gd, seedlings)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(seedlings));
        assertThat(gqs.getEffectiveCardToughness(gd, seedlings)).isEqualTo(2);
    }

    private Permanent addReady(Player player) {
        return addCreatureReady(player, new TreefolkSeedlings());
    }
}
