package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.g.GhituEncampment;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    @DisplayName("Colors return immediately when the last Lens leaves the battlefield")
    void colorsReturnWhenLensLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AuraFlux());
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new ThranLens());

        assertThat(gqs.getEffectiveColors(gd, creature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, enchantment)).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lens));

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, enchantment)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Removing one of two Lenses does not restore colors")
    void anotherLensKeepsPermanentsColorless() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());
        Permanent firstLens = harness.addToBattlefieldAndReturn(player1, new ThranLens());
        Permanent secondLens = harness.addToBattlefieldAndReturn(player2, new ThranLens());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstLens));
        assertThat(gqs.getEffectiveColors(gd, creature)).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondLens));
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A later animation can make a land red while the Lens remains")
    void laterAnimationOverridesLensColorEffect() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhituEncampment());
        harness.addToBattlefield(player1, new ThranLens());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("A later Lens makes an already animated land colorless")
    void laterLensOverridesAnimationColorEffect() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.RED);

        harness.castFromHand(player1, new ThranLens(), "{2}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, land)).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("A creature spell retains its color until it becomes a permanent")
    void creatureSpellKeepsItsColorOnStack() {
        harness.addToBattlefield(player1, new ThranLens());
        harness.castFromHand(player1, new YavimayaWurm(), "{4}{G}{G}");

        assertThat(gqs.getEffectiveCardColors(gd, gd.stack.getFirst().getCard()))
                .containsExactly(CardColor.GREEN);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, findPermanent(player1, "Yavimaya Wurm"))).isEmpty();
    }
}
