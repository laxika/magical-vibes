package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.g.GhituEncampment;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranLens.class, YavimayaWurm.class, AuraFlux.class, GhituEncampment.class})
class ThranLensTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every battlefield permanent colorless")
    void makesEveryBattlefieldPermanentColorless() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AuraFlux());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhituEncampment());
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new ThranLens());

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opponentCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, enchantment)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, land)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, lens)).isEmpty();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        assertThat(gqs.getEffectiveColors(gd, laterCreature)).isEmpty();
    }

    @Test
    @DisplayName("Does not make cards outside the battlefield colorless")
    void leavesCardsOutsideBattlefieldUnchanged() {
        harness.addToBattlefield(player1, new ThranLens());
        harness.setHand(player1, List.of(new YavimayaWurm()));

        assertThat(gqs.getEffectiveCardColors(gd, gd.playerHands.get(player1.getId()).getFirst()))
                .containsExactly(CardColor.GREEN);
    }
}
