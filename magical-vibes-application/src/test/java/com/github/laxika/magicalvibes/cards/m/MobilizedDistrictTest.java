package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.k.KrenkoTinStreetKingpin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobilizedDistrict.class, KrenkoTinStreetKingpin.class, GideonBlackblade.class})
class MobilizedDistrictTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mobilized District produces colorless mana")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new MobilizedDistrict());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mobilized District becomes a vigilant 3/3 Citizen that remains a land")
    void animatesIntoCitizen() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, district)).isTrue();
        assertThat(gqs.getEffectivePower(gd, district)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, district)).isEqualTo(3);
        assertThat(district.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(district.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Legendary creatures and planeswalkers reduce the animation cost")
    void legendaryCreaturesAndPlaneswalkersReduceActivationCost() {
        harness.addToBattlefield(player1, new MobilizedDistrict());
        harness.addToBattlefield(player1, new KrenkoTinStreetKingpin());
        harness.addToBattlefield(player1, new GideonBlackblade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mobilized District's animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, district)).isFalse();
    }
}
