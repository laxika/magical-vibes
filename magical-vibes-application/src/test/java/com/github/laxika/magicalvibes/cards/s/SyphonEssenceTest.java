package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyphonEssence.class, GrizzlyBears.class, JaceBeleren.class, Millstone.class})
class SyphonEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and creates a Blood token")
    void countersCreatureSpellAndCreatesBlood() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Blood")).isOne();
    }

    @Test
    @DisplayName("Counters a planeswalker spell and creates a Blood token")
    void countersPlaneswalkerSpellAndCreatesBlood() {
        JaceBeleren jace = new JaceBeleren();
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, jace.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        assertThat(countPermanents(player2, "Blood")).isOne();
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetNoncreatureNonplaneswalkerSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
