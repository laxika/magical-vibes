package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzzarootChanneler.class, GrizzlyBears.class, Divination.class})
class EzzarootChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells cost less by the amount of life gained this turn")
    void reducesCreatureSpellCostByLifeGained() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The reduction does not apply to noncreature spells")
    void doesNotReduceNoncreatureSpellCost() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability makes its controller gain 2 life")
    void gainsTwoLifeWhenTapped() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new EzzarootChanneler());
        channeler.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
