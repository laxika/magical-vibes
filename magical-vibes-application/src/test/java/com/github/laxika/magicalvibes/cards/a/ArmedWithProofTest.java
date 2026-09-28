package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmedWithProof.class, GrizzlyBears.class})
class ArmedWithProofTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates twice when it enters the battlefield")
    void investigatesTwiceWhenItEnters() {
        castArmedWithProof();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Clues you control become Equipment and can equip creatures")
    void cluesBecomeEquipmentAndCanEquipCreatures() {
        castArmedWithProof();
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(clue.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void castArmedWithProof() {
        harness.setHand(player1, List.of(new ArmedWithProof()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
