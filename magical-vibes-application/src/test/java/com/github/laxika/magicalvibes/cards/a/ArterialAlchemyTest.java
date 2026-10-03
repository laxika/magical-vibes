package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArterialAlchemy.class, GrizzlyBears.class})
class ArterialAlchemyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one Blood token for each opponent")
    void createsBloodTokenForEachOpponent() {
        harness.setHand(player1, List.of(new ArterialAlchemy()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
        assertThat(bloods.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(bloods.getFirst().getCard().getSubtypes()).contains(CardSubtype.BLOOD);
    }

    @Test
    @DisplayName("Blood tokens you control become Equipment with equip {2} and +2/+0")
    void bloodTokensGainEquipmentAbilities() {
        resolveAlchemy();
        Permanent blood = findPermanent(player1, "Blood");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        StaticBonus bonus = gqs.computeStaticBonus(gd, blood);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.EQUIPMENT);
        assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
        assertThat(bonus.grantedActivatedAbilities().getFirst().getManaCost()).isEqualTo("{2}");

        blood.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void resolveAlchemy() {
        harness.setHand(player1, List.of(new ArterialAlchemy()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
