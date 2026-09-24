package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninBola;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuriokGlaivemaster.class, LeoninBola.class})
class AuriokGlaivemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Without equipment, Auriok Glaivemaster is a 1/1 without first strike")
    void withoutEquipmentHasNoBonus() {
        Permanent glaivemaster = addCreatureReady(player1, new AuriokGlaivemaster());

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("With equipment attached, Auriok Glaivemaster gets +1/+1 and first strike")
    void withEquipmentHasBonus() {
        Permanent glaivemaster = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        equipment.setAttachedTo(glaivemaster.getId());

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipment controlled by another player still equips Auriok Glaivemaster")
    void equipmentControlledByOpponentStillGrantsBonus() {
        Permanent glaivemaster = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninBola());
        equipment.setAttachedTo(glaivemaster.getId());

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("When equipment is detached, Auriok Glaivemaster loses the bonus")
    void afterEquipmentDetachedLosesBonus() {
        Permanent glaivemaster = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        equipment.setAttachedTo(glaivemaster.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isFalse();
    }
}
