package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic land taps for red instead of its normal mana")
    void nonbasicLandProducesRed() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new BloodMoon());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land's subtypes are overridden to Mountain")
    void nonbasicLandSubtypesOverriddenToMountain() {
        harness.addToBattlefield(player1, new Glimmerpost());
        Permanent glimmerpost = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new BloodMoon());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, glimmerpost);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Basic land is unaffected — still produces its normal mana")
    void basicLandUnaffected() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BloodMoon());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic land's subtypes are not overridden")
    void basicLandSubtypesNotOverridden() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new BloodMoon());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.landSubtypeOverriding()).isFalse();
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Nonbasic land produces its normal mana once Blood Moon leaves")
    void normalManaResumesWhenBloodMoonLeaves() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new BloodMoon());
        Permanent bloodMoon = gd.playerBattlefields.get(player1.getId()).get(1);

        gd.playerBattlefields.get(player1.getId()).remove(bloodMoon);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }
}
