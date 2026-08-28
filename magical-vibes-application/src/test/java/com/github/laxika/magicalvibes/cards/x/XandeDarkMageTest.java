package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XandeDarkMage.class, Shock.class, Forest.class, GrizzlyBears.class})
class XandeDarkMageTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each noncreature, nonland card in its controller's graveyard")
    void getsBoostFromQualifyingGraveyardCards() {
        Permanent xande = addXandeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Forest(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts only its controller's qualifying graveyard cards")
    void ignoresOtherCardTypesAndOpponentsGraveyard() {
        Permanent xande = addXandeReady(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(4);
    }

    @Test
    @DisplayName("Updates as qualifying cards enter or leave the graveyard")
    void updatesDynamically() {
        Permanent xande = addXandeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Forest(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(new Shock(), new Forest(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);
    }

    private Permanent addXandeReady(Player player) {
        Permanent permanent = new Permanent(new XandeDarkMage());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
