package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.w.WeatherseedFaeries;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantArchangel.class, AngelicCurator.class, WeatherseedFaeries.class,
        GiantCockroach.class, FaerieConclave.class, Levitation.class})
class RadiantArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other creature with flying on the battlefield")
    void boostsForOtherFlyingCreaturesOnBattlefield() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        harness.addToBattlefield(player1, new AngelicCurator());
        harness.addToBattlefield(player2, new WeatherseedFaeries());
        harness.addToBattlefield(player1, new GiantCockroach());

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, radiant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates as other flying creatures enter and leave")
    void updatesDynamically() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);

        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new AngelicCurator());
        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(hawk);
        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);
    }

    @Test
    void countsAnimatedFlyingLand() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        harness.addToBattlefield(player2, new FaerieConclave());

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, radiant)).isEqualTo(4);
    }

    @Test
    void opposingRadiantsCountEachOtherWithoutCountingThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RadiantArchangel());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void ignoresFlyingCreatureCardsOutsideTheBattlefield() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        harness.setHand(player1, List.of(new AngelicCurator()));
        harness.setLibrary(player2, List.of(new WeatherseedFaeries()));
        harness.setGraveyard(player2, List.of(new AngelicCurator()));
        harness.setExile(player1, List.of(new WeatherseedFaeries()));

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, radiant)).isEqualTo(3);
    }

    @Test
    void countsFlyingGrantedByAnOpponentsEnchantment() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        harness.addToBattlefield(player2, new GiantCockroach());
        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Levitation(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, radiant)).isEqualTo(4);
    }
}
