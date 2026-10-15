package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObsidianCharmaw.class, Forest.class, GhostQuarter.class, Wastes.class})
class ObsidianCharmawTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less for each opponent land that could produce colorless mana")
    void costsLessForOpponentsColorlessProducingLands() {
        harness.addToBattlefield(player2, new Wastes());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not count lands controlled by the controller or lands unable to produce colorless mana")
    void costReductionOnlyCountsOpponentsColorlessProducingLands() {
        harness.addToBattlefield(player1, new Wastes());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enters and destroys target nonbasic land an opponent controls")
    void entersAndDestroysTargetNonbasicOpponentLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a basic land or a land controlled by the controller")
    void cannotTargetBasicOrOwnLand() {
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, basicLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land an opponent controls");
    }

    @Test
    @DisplayName("Tapped opponent lands still reduce the casting cost")
    void tappedLandsStillReduceCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wastes());
        land.tap();
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess cost reduction leaves the two red mana requirement")
    void excessReductionDoesNotReduceColoredCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Wastes());
        }
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
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
    @DisplayName("Can enter when the opponent controls only basic lands")
    void entersWithoutLegalLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Obsidian Charmaw");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonbasic land controlled by the caster")
    void cannotTargetOwnNonbasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());
        harness.setHand(player1, List.of(new ObsidianCharmaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land an opponent controls");
    }
}
