package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DomesticatedMammoth.class})
class DomesticatedMammothTest extends BaseCardTest {

    @Test
    void entersWithPacifismTokenAttached() {
        harness.castFromHand(player1, new DomesticatedMammoth(), "{1}{G}");
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

    @Test
    void pacifismIsAlreadyAttachedWhenCreatureSpellResolves() {
        harness.castFromHand(player1, new DomesticatedMammoth(), "{1}{G}");
        harness.passBothPriorities();

        Permanent mammoth = findPermanent(player1, "Domesticated Mammoth");
        Permanent pacifism = findPermanent(player1, "Pacifism");

        assertThat(pacifism.getAttachedTo()).isEqualTo(mammoth.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(bls.canBlock(gd, mammoth)).isFalse();
    }

    @Test
    void pacifismCopyRetainsItsManaCost() {
        harness.castFromHand(player1, new DomesticatedMammoth(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pacifism = findPermanent(player1, "Pacifism");

        assertThat(pacifism.getCard().getManaCost()).isEqualTo("{1}{W}");
        assertThat(pacifism.getCard().getManaValue()).isEqualTo(2);
    }

    @Test
    void removingPacifismAllowsMammothToAttackAndBlock() {
        harness.castFromHand(player1, new DomesticatedMammoth(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mammoth = findPermanent(player1, "Domesticated Mammoth");
        Permanent pacifism = findPermanent(player1, "Pacifism");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pacifism));
        harness.runStateBasedActions();

        mammoth.setSummoningSick(false);
        assertThat(als.canAttack(gd, mammoth, player1.getId())).isTrue();
        assertThat(bls.canBlock(gd, mammoth)).isTrue();
    }
}
