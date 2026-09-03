package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EarlyHarvest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnyaroGriffin.class, Incinerate.class, EarlyHarvest.class, GoblinEliteInfantry.class,
        StoneRain.class, Forest.class})
class UnyaroGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a red instant spell, sacrificing itself as a cost")
    void countersRedInstantSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, incinerate.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Incinerate");
        // Unyaro Griffin sacrificed as a cost
        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertNotOnBattlefield(player1, "Unyaro Griffin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a green instant spell")
    void cannotTargetGreenInstant() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        EarlyHarvest earlyHarvest = new EarlyHarvest();
        harness.setHand(player2, List.of(earlyHarvest));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, earlyHarvest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a red creature spell (not an instant or sorcery)")
    void cannotTargetRedCreatureSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        GoblinEliteInfantry goblin = new GoblinEliteInfantry();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a red sorcery spell, sacrificing itself as a cost")
    void countersRedSorcerySpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        harness.addToBattlefield(player2, new Forest());

        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, harness.getPermanentId(player2, "Forest"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
