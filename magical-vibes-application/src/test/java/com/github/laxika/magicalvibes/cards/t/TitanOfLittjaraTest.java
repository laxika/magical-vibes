package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanOfLittjara.class, GrizzlyBears.class, Forest.class})
class TitanOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("Titan becomes the chosen type and draws for other matching creatures on entry")
    void entersAndDrawsForOtherMatchingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new TitanOfLittjara())));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, discard.validIndices().getFirst());

        Permanent titan = findPermanent(player1, "Titan of Littjara");
        assertThat(titan.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.computeStaticBonus(gd, titan).grantedSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the entry trigger does not draw or discard")
    void decliningEntryTriggerDoesNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new TitanOfLittjara())));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Titan of Littjara"))
                .grantedSubtypes()).contains(CardSubtype.BEAR);
    }
}
