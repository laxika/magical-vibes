package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PetrifiedFieldTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new PetrifiedField());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{T}, Sacrifice this land returns a target land card from the graveyard to hand")
    void sacrificesAndReturnsTargetLand() {
        harness.addToBattlefield(player1, new PetrifiedField());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbility(player1, 0, 1, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Petrified Field");
        harness.assertNotOnBattlefield(player1, "Petrified Field");
    }

    @Test
    @DisplayName("The ability cannot target a nonland card")
    void cannotTargetNonlandCard() {
        harness.addToBattlefield(player1, new PetrifiedField());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        Card creature = new CopperMyr();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
