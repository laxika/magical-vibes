package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VituGhaziTheCityTree;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManascapeRefractor.class, Forest.class, VituGhaziTheCityTree.class, BloodMoon.class})
class ManascapeRefractorTest extends BaseCardTest {

    @Test
    void entersTapped() {
        Permanent refractor = harness.enterBattlefieldAndReturn(player1, new ManascapeRefractor());

        assertThat(refractor.isTapped()).isTrue();
    }

    @Test
    void copiesManaAbilitiesFromLandsOnEitherBattlefield() {
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new Forest());
        refractor.untap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(refractor.isTapped()).isTrue();
    }

    @Test
    void copiesColoredActivatedAbilitiesAndPaysTheirCostsWithAnyColorMana() {
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());
        refractor.untap();
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
    }

    @Test
    void gainsIntrinsicManaAbilityOfLandChangedToMountain() {
        harness.addToBattlefield(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());
        harness.addToBattlefield(player2, new BloodMoon());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void doesNotGainPrintedAbilityRemovedByLandTypeChange() {
        harness.addToBattlefield(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());
        harness.addToBattlefield(player2, new BloodMoon());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    void losesAbilityWhenItsOnlyDonorLeavesBattlefield() {
        harness.addToBattlefield(player1, new ManascapeRefractor());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void activatedAbilityResolvesAfterDonorLeavesBattlefield() {
        harness.addToBattlefield(player1, new ManascapeRefractor());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
    }

    @Test
    void cannotActivateAnotherTapAbilityWhileAlreadyTapped() {
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(refractor.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Saproling");
    }
}
