package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfBambooGroves.class, Forest.class})
class KamiOfBambooGrovesTest extends BaseCardTest {

    @Test
    void entersAndMayPutAHandLandOntoTheBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new KamiOfBambooGroves(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredForest = findPermanent(forest);
        assertThat(enteredForest).isNotNull();
        assertThat(enteredForest.isTapped()).isTrue();
    }

    @Test
    void channelDiscardsKamiAndConjuresTwoForests() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KamiOfBambooGroves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
        harness.assertInGraveyard(player1, "Kami of Bamboo Groves");
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
