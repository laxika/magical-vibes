package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakashimaOfAThousandFaces.class, GrizzlyBears.class, TsaboTavoc.class})
class SakashimaOfAThousandFacesTest extends BaseCardTest {

    @Test
    void onlyOffersCreaturesTheControllerControls() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SakashimaOfAThousandFaces card = castSakashima();

        resolveCopyChoice(card, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());

        Permanent copy = findSakashima(card);
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void copiedSakashimaStillIgnoresTheLegendRuleForItsController() {
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        SakashimaOfAThousandFaces card = castSakashima();

        resolveCopyChoice(card, legendaryCreature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private SakashimaOfAThousandFaces castSakashima() {
        SakashimaOfAThousandFaces card = new SakashimaOfAThousandFaces();
        harness.castFromHand(player1, card, "{3}{U}");
        return card;
    }

    private void resolveCopyChoice(SakashimaOfAThousandFaces card, Permanent target) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
        }
    }

    private Permanent findSakashima(SakashimaOfAThousandFaces card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
