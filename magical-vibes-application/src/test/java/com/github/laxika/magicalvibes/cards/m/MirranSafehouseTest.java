package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlastedLandscape;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.StripMine;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirranSafehouseTest extends BaseCardTest {

    @Test
    void gainsAbilitiesFromLandCardsInAllGraveyards() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new BlastedLandscape())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new StripMine())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, safehouse).grantedActivatedAbilities();

        assertThat(granted).hasSize(3);
    }

    @Test
    void includesBasicLandTapAbilities() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Forest())));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(safehouse.isTapped()).isTrue();
    }

    @Test
    void ignoresNonlandCards() {
        Permanent safehouse = addSafehouse();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new RodOfRuin())));

        assertThat(gqs.computeStaticBonus(gd, safehouse).grantedActivatedAbilities()).isEmpty();
    }

    private Permanent addSafehouse() {
        return harness.addToBattlefieldAndReturn(player1, new MirranSafehouse());
    }
}
