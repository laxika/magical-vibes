package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrimBauble;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourTheScene.class, GrimBauble.class, GrizzlyBears.class})
class ScourTheSceneTest extends BaseCardTest {

    @Test
    void createsWhiteBlueDetectiveOnEntry() {
        harness.setHand(player1, List.of(new ScourTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getEffectivePower()).isEqualTo(2);
        assertThat(detective.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void sacrificeArtifactCanPerpetuallyBoostCreatureCardInHand() {
        harness.addToBattlefield(player1, new ScourTheScene());
        harness.addToBattlefield(player1, new GrimBauble());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        addSacrificeMana();

        sacrificeGrimBauble();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardOrPermanentChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getEffectivePower()).isEqualTo(3);
        assertThat(entered.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void sacrificeArtifactCanPerpetuallyBoostCreatureYouControl() {
        harness.addToBattlefield(player1, new ScourTheScene());
        harness.addToBattlefield(player1, new GrimBauble());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSacrificeMana();

        sacrificeGrimBauble();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardOrPermanentChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void sacrificeGrimBauble() {
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void addSacrificeMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
