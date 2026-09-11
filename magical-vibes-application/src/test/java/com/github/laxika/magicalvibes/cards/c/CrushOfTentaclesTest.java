package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushOfTentacles.class, GrizzlyBears.class, HillGiant.class, Island.class})
class CrushOfTentaclesTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting returns all nonland permanents and creates no Octopus")
    void normalCastReturnsNonlandsWithoutToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new CrushOfTentacles()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(findPermanents(player1, "Octopus")).isEmpty();
    }

    @Test
    @DisplayName("Surge casting returns nonlands and creates an 8/8 blue Octopus")
    void surgeCastReturnsNonlandsAndCreatesOctopus() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new CrushOfTentacles()));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");

        Permanent octopus = findPermanent(player1, "Octopus");
        assertThat(octopus.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(octopus.getCard().getSubtypes()).containsExactly(CardSubtype.OCTOPUS);
        assertThat(octopus.getEffectivePower()).isEqualTo(8);
        assertThat(octopus.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("The surge alternate cost requires another spell cast this turn")
    void surgeCostRequiresAnotherSpell() {
        harness.setHand(player1, List.of(new CrushOfTentacles()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
