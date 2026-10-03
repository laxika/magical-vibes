package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmadaWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaruWurmspeaker.class, ArmadaWurm.class, GrizzlyBears.class})
class BaruWurmspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Wurms you control get +2/+2 and trample")
    void boostsOwnWurms() {
        Permanent baru = addCreatureReady(player1, new BaruWurmspeaker());
        Permanent wurm = addCreatureReady(player1, new ArmadaWurm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentWurm = addCreatureReady(player2, new ArmadaWurm());

        assertThat(gqs.getEffectivePower(gd, baru)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, baru)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, baru, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentWurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activates for only the colored mana when a Wurm has power seven")
    void createsWurmWithGreatestPowerCostReduction() {
        Permanent baru = addCreatureReady(player1, new BaruWurmspeaker());
        addCreatureReady(player1, new ArmadaWurm());
        harness.addMana(player1, ManaColor.GREEN, 1);

        int baruIndex = gd.playerBattlefields.get(player1.getId()).indexOf(baru);
        harness.activateAbility(player1, baruIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void paysFullCostWithoutOwnWurmsAndTokenLosesBoostWhenBaruLeaves() {
        Permanent baru = addCreatureReady(player1, new BaruWurmspeaker());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThat(baru.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Wurm");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(baru);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void ownTokenReducesNextActivationToOneGenericAndOneGreen() {
        Permanent baru = addCreatureReady(player1, new BaruWurmspeaker());
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        baru.untap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wurm")).isEqualTo(2);
    }

    @Test
    void opposingWurmDoesNotReduceActivationCost() {
        addCreatureReady(player1, new BaruWurmspeaker());
        addCreatureReady(player2, new ArmadaWurm());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Wurm")).isZero();
    }

    @Test
    void reductionCannotPayColoredMana() {
        addCreatureReady(player1, new BaruWurmspeaker());
        addCreatureReady(player1, new ArmadaWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BaruWurmspeaker());
        harness.addMana(player1, ManaColor.GREEN, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateTwiceWithoutUntapping() {
        addCreatureReady(player1, new BaruWurmspeaker());
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Wurm")).isEqualTo(1);
    }
}
