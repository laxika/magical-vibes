package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.i.IlysianCaryatid;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.s.SatyrHedonist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxbloomAncient.class, Forest.class, ManaReflection.class,
        IlysianCaryatid.class, SatyrHedonist.class, Ichthyomorphosis.class})
class NyxbloomAncientTest extends BaseCardTest {

    @Test
    void triplesManaFromYourPermanent() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void doesNotTripleManaFromAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void multipleNyxbloomAncientsStackMultiplicatively() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(9);
    }

    @Test
    void stacksMultiplicativelyWithManaReflection() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(6);
    }

    @Test
    void triplesMultipleManaOfTheChosenColorFromACreature() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        var caryatid = addCreatureReady(player1, new IlysianCaryatid());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(caryatid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTripleManaFromAnAbilityWithoutATapSymbolCost() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Satyr Hedonist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stopsTriplingManaWhenItLosesItsAbilities() {
        var ancient = harness.addToBattlefieldAndReturn(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, ancient.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
