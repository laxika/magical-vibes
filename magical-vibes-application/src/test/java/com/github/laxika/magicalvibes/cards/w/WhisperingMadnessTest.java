package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperingMadnessTest extends BaseCardTest {

    @Test
    @DisplayName("Every player draws equal to the largest hand discarded")
    void everyoneDrawsGreatestDiscarded() {
        harness.setHand(player1, List.of(new WhisperingMadness(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Whispering Madness");
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.setHand(player1, List.of(new WhisperingMadness()));
        harness.setHand(player2, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Whispering Madness"));
        harness.assertNotInGraveyard(player1, "Whispering Madness");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.exiledCards).hasSize(1);
    }
}
