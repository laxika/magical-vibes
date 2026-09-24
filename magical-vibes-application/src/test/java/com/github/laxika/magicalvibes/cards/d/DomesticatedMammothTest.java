package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DomesticatedMammoth.class)
class DomesticatedMammothTest extends BaseCardTest {

    @Test
    void entersWithPacifismTokenAttached() {
        harness.setHand(player1, List.of(new DomesticatedMammoth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mammoth = findPermanent(player1, "Domesticated Mammoth");
        Permanent pacifism = findPermanent(player1, "Pacifism");

        assertThat(pacifism.getCard().isToken()).isTrue();
        assertThat(pacifism.getCard().isAura()).isTrue();
        assertThat(pacifism.getCard().getSubtypes()).contains(CardSubtype.AURA);
        assertThat(pacifism.getAttachedTo()).isEqualTo(mammoth.getId());

        mammoth.setSummoningSick(false);
        assertThat(als.canAttack(gd, mammoth, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, mammoth)).isFalse();
    }
}
