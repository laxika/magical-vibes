package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaxosTheReturned.class, Bitterblossom.class})
class DaxosTheReturnedTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenControllerCastsAnEnchantment() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.setHand(player1, List.of(new Bitterblossom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void createsSpiritEnchantmentTokenWhosePowerAndToughnessFollowExperience() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);

        gd.playerExperienceCounters.put(player1.getId(), 5);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }
}
