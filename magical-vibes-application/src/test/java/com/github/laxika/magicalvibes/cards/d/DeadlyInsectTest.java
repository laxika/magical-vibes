package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyInsect.class, Vendetta.class, ShockTroops.class})
class DeadlyInsectTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Deadly Insect")
    void spellsCannotTargetDeadlyInsect() {
        Permanent insect = addCreatureReady(player1, new DeadlyInsect());
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, insect.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents abilities from targeting Deadly Insect")
    void abilitiesCannotTargetDeadlyInsect() {
        addCreatureReady(player1, new ShockTroops());
        Permanent insect = addCreatureReady(player1, new DeadlyInsect());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, insect.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
