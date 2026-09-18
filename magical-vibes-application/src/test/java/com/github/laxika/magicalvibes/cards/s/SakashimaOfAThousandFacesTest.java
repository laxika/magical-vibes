package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakashimaOfAThousandFaces.class, KondaLordOfEiganjo.class})
class SakashimaOfAThousandFacesTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature and keeps its legend-rule exemption")
    void copiesCreatureAndKeepsLegendRuleExemption() {
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.castFromHand(player1, new SakashimaOfAThousandFaces(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(
                player1, harness.getPermanentId(player1, "Konda, Lord of Eiganjo"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.getCard().getName().equals("Konda, Lord of Eiganjo"));
    }

    @Test
    @DisplayName("Does not offer an opponent's creature as a copy choice")
    void onlyCopiesControlledCreatures() {
        harness.addToBattlefield(player2, new KondaLordOfEiganjo());
        harness.castFromHand(player1, new SakashimaOfAThousandFaces(), "{3}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sakashima of a Thousand Faces");
        harness.assertOnBattlefield(player2, "Konda, Lord of Eiganjo");
    }

    @Test
    @DisplayName("Only exempts legendary permanents controlled by Sakashima's controller")
    void exemptionIsControllerScoped() {
        harness.addToBattlefield(player1, new SakashimaOfAThousandFaces());
        harness.addToBattlefield(player2, new KondaLordOfEiganjo());
        harness.addToBattlefield(player2, new KondaLordOfEiganjo());

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }
}
