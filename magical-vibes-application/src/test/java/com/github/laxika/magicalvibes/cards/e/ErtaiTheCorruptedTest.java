package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErtaiTheCorruptedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell by sacrificing Ertai")
    void countersSpellBySacrificingItself() {
        Permanent ertai = addCreatureReady(player1, new ErtaiTheCorrupted());
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ertai.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ertai);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice an enchantment instead of Ertai")
    void countersSpellBySacrificingEnchantment() {
        Permanent ertai = addCreatureReady(player1, new ErtaiTheCorrupted());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, anthem.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ertai).doesNotContain(anthem);
        assertThat(ertai.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(anthem.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.stack).isEmpty();
    }
}
