package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakashimaOfAThousandFaces.class, KondaLordOfEiganjo.class, Frogify.class})
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

    @Test
    @DisplayName("Retains partner when copying a creature without partner")
    void retainsPartnerWhenCopying() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KondaLordOfEiganjo());
        harness.castFromHand(player1, new SakashimaOfAThousandFaces(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.PARTNER)).isTrue();
    }

    @Test
    @DisplayName("Can decline copying and still exempt other legendary permanents")
    void decliningCopyKeepsLegendRuleExemption() {
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.castFromHand(player1, new SakashimaOfAThousandFaces(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sakashima of a Thousand Faces");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Losing Sakashima's abilities restores the legend rule")
    void losingAbilitiesRestoresLegendRule() {
        Permanent sakashima = harness.addToBattlefieldAndReturn(player1, new SakashimaOfAThousandFaces());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        harness.runStateBasedActions();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, sakashima.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }
}
