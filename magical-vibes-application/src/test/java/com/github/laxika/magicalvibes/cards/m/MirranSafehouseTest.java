package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlastedLandscape;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.StripMine;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirranSafehouse.class, BlastedLandscape.class, Forest.class, RodOfRuin.class, StripMine.class})
class MirranSafehouseTest extends BaseCardTest {

    @Test
    @CardUsed({BlastedLandscape.class, StripMine.class})
    void gainsAbilitiesFromLandCardsInAllGraveyards() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, List.of(new BlastedLandscape()));
        harness.setGraveyard(player2, List.of(new StripMine()));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, safehouse).grantedActivatedAbilities();

        assertThat(granted).hasSize(4);
    }

    @Test
    @CardUsed(Forest.class)
    void includesBasicLandTapAbilities() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(safehouse.isTapped()).isTrue();
    }

    @Test
    @CardUsed(RodOfRuin.class)
    void ignoresNonlandCards() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));

        assertThat(gqs.computeStaticBonus(gd, safehouse).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @CardUsed({StripMine.class, Forest.class})
    void inheritedSacrificeCostSacrificesSafehouseAndDestroysTargetLand() {
        addSafehouse();
        StripMine stripMine = new StripMine();
        harness.setGraveyard(player2, List.of(stripMine));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Mirran Safehouse");
        harness.assertInGraveyard(player1, "Mirran Safehouse");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stripMine);
        harness.assertOnBattlefield(player2, "Forest");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stripMine);
    }

    @Test
    @CardUsed(Forest.class)
    void losesInheritedAbilityWhenLandLeavesGraveyard() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        safehouse.setTapped(false);
        harness.setGraveyard(player2, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(safehouse.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addSafehouse() {
        return harness.addToBattlefieldAndReturn(player1, new MirranSafehouse());
    }
}
