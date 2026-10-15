package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrumpetingHerd.class})
class TrumpetingHerdTest extends BaseCardTest {

    @Test
    void createsElephantTokenAndExilesForRebound() {
        TrumpetingHerd card = new TrumpetingHerd();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> elephants = elephantTokens();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersFreeCastAtNextUpkeep() {
        TrumpetingHerd card = new TrumpetingHerd();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(elephantTokens()).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Trumpeting Herd");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundWaitsForControllersUpkeep() {
        TrumpetingHerd card = new TrumpetingHerd();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(elephantTokens()).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(elephantTokens()).hasSize(2);
        harness.assertInGraveyard(player1, "Trumpeting Herd");
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOpportunity() {
        TrumpetingHerd card = new TrumpetingHerd();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elephantTokens()).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Trumpeting Herd");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elephantTokens()).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Permanent> elephantTokens() {
        return findPermanents(player1, "Elephant").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
