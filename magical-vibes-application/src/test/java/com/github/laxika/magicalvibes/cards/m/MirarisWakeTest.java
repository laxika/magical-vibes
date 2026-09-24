package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrystalQuarry;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystalQuarry.class, KrosanVerge.class, MirarisWake.class, MossfireValley.class, SuntailHawk.class})
class MirarisWakeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures you control +1/+1")
    void boostsCreaturesYouControl() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player1, new SuntailHawk());

        Permanent hawk = findPermanent(player1, "Suntail Hawk");

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not give the bonus to an opponent's creatures")
    void doesNotBoostOpponentsCreatures() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player2, new SuntailHawk());

        Permanent hawk = findPermanent(player2, "Suntail Hawk");

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds one additional mana when you tap a land for mana")
    void addsManaForYourLandTap() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player1, new KrosanVerge());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add mana when an opponent taps a land")
    void doesNotAddManaForOpponentsLandTap() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player2, new KrosanVerge());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @CardUsed(CrystalQuarry.class)
    @DisplayName("Adds only one additional mana when a land produces multiple colors")
    void addsOnlyOneManaWhenLandProducesMultipleColors() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player1, new CrystalQuarry());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds only one additional mana when a land produces multiple types")
    void addsOnlyOneManaForLandProducingMultipleTypes() {
        harness.addToBattlefield(player1, new MirarisWake());
        harness.addToBattlefield(player1, new MossfireValley());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.COLORLESS)).isZero();
        assertThat(manaPool.get(ManaColor.RED) + manaPool.get(ManaColor.GREEN)).isEqualTo(3);
    }
}
