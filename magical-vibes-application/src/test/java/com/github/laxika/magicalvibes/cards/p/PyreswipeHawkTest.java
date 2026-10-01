package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreswipeHawk.class, GildedLotus.class, Shock.class})
class PyreswipeHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 on attack for the greatest mana value among artifacts you control")
    void boostsByGreatestControlledArtifactManaValue() {
        Permanent hawk = addCreatureReady(player1, new PyreswipeHawk());
        harness.addToBattlefield(player1, new GildedLotus());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gains control of up to one target artifact when its controller expends six")
    void gainsControlOfTargetArtifactWhenControllerExpendsSix() {
        Permanent hawk = addCreatureReady(player1, new PyreswipeHawk());
        Permanent lotus = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        castShocks(5);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, lotus.getId());
        resolveAllTriggers();

        assertThat(gqs.findPermanentController(gd, lotus.getId())).isEqualTo(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hawk));

        assertThat(gqs.findPermanentController(gd, lotus.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Does not trigger before its controller expends six")
    void doesNotTriggerBeforeControllerExpendsSix() {
        addCreatureReady(player1, new PyreswipeHawk());
        Permanent lotus = harness.addToBattlefieldAndReturn(player2, new GildedLotus());

        castShocks(5);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.findPermanentController(gd, lotus.getId())).isEqualTo(player2.getId());
    }

    private void castShocks(int count) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        List<Card> shocks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shocks.add(new Shock());
        }
        harness.setHand(player1, shocks);
        harness.addMana(player1, ManaColor.RED, count);
        for (int i = 0; i < count; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }
    }
}
