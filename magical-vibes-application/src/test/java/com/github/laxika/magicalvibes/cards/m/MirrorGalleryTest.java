package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorGalleryTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate legendary permanents survive while Mirror Gallery is on the battlefield")
    void duplicateLegendsSurvive() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Mirror Gallery protects duplicate legends controlled by another player")
    void protectsLegendsControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new MirrorGallery());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("The legend rule returns when Mirror Gallery leaves the battlefield")
    void legendRuleReturnsWhenGalleryLeaves() {
        Permanent gallery = harness.addToBattlefieldAndReturn(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        gd.playerBattlefields.get(player1.getId()).remove(gallery);

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }
}
