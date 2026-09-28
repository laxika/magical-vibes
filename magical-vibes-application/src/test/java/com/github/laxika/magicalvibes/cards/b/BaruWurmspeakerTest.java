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

@CardUsed({BaruWurmspeaker.class, ArmadaWurm.class, GrizzlyBears.class})
class BaruWurmspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Wurms you control get +2/+2 and trample")
    void boostsOwnWurms() {
        Permanent baru = addReady(player1, new BaruWurmspeaker());
        Permanent wurm = addReady(player1, new ArmadaWurm());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentWurm = addReady(player2, new ArmadaWurm());

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
        Permanent baru = addReady(player1, new BaruWurmspeaker());
        addReady(player1, new ArmadaWurm());
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

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
