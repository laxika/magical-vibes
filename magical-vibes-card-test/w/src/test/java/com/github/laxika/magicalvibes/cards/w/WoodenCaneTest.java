package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodenCane.class, GrizzlyBears.class})
class WoodenCaneTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Wooden Cane creates a red Mutant token, attaches to it, and boosts it")
    void enteringCreatesAndAttachesMutant() {
        harness.castFromHand(player1, new WoodenCane(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cane = findPermanent(player1, "Wooden Cane");
        Permanent mutant = findPermanent(player1, "Mutant");
        assertThat(mutant.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(mutant.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
        assertThat(cane.getAttachedTo()).isEqualTo(mutant.getId());
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {3} attaches Wooden Cane to a creature you control")
    void equipAttachesToCreature() {
        Permanent cane = addCreatureReady(player1, new WoodenCane());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(cane.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each entering Cane attaches only itself to its own new token")
    void multipleCanesKeepSeparateAttachments() {
        harness.castFromHand(player1, new WoodenCane(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstCane = findPermanent(player1, "Wooden Cane");
        Permanent firstMutant = findPermanent(player1, "Mutant");

        harness.castFromHand(player1, new WoodenCane(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent secondCane = findPermanents(player1, "Wooden Cane").get(1);
        Permanent secondMutant = findPermanents(player1, "Mutant").get(1);
        assertThat(countPermanents(player1, "Mutant")).isEqualTo(2);
        assertThat(firstCane.getAttachedTo()).isEqualTo(firstMutant.getId());
        assertThat(secondCane.getAttachedTo()).isEqualTo(secondMutant.getId());
        assertThat(gqs.getEffectivePower(gd, firstMutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstMutant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondMutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondMutant)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, secondMutant.getId());
        harness.passBothPriorities();

        assertThat(firstCane.getAttachedTo()).isEqualTo(secondMutant.getId());
        assertThat(gqs.getEffectivePower(gd, firstMutant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstMutant)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondMutant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, secondMutant)).isEqualTo(4);
        assertThat(countPermanents(player1, "Mutant")).isEqualTo(2);
    }
}
