package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({MistformWarchief.class, MistformDreamer.class, GrizzlyBears.class, HillGiant.class})
class MistformWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells sharing this creature's type cost {1} less")
    void reducesCreatureSpellsSharingType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new MistformDreamer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MistformDreamer.class);
    }

    @Test
    @DisplayName("Creature spells without a shared type are not reduced")
    void doesNotReduceCreatureSpellsWithoutSharedType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing this creature's type changes which creature spells are reduced")
    void usesCurrentCreatureTypeForCostReduction() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new MistformWarchief());
        warchief.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GIANT.name());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(HillGiant.class);
    }
}
