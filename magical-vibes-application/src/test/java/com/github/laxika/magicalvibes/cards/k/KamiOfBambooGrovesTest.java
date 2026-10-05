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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfBambooGroves.class, Forest.class})
class KamiOfBambooGrovesTest extends BaseCardTest {

    @Test
    void entersAndMayPutAHandLandOntoTheBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new KamiOfBambooGroves(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredForest = findPermanent(player1, "Forest");
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

    @Test
    void mayDeclinePuttingALandOntoTheBattlefield() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new KamiOfBambooGroves(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingWithOnlyANonlandInHandDoesNotPutItOntoTheBattlefield() {
        KamiOfBambooGroves otherKami = new KamiOfBambooGroves();
        harness.setHand(player1, List.of(new KamiOfBambooGroves(), otherKami));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherKami);
        assertThat(countPermanents(player1, "Kami of Bamboo Groves")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void channelWorksDuringOpponentsTurnAndDiscardsBeforeResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new KamiOfBambooGroves()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kami of Bamboo Groves");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Forest", "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).doesNotContain("Forest");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).doesNotHaveDuplicates();
    }

    @Test
    void channelCannotBeActivatedWithoutEnoughMana() {
        KamiOfBambooGroves kami = new KamiOfBambooGroves();
        harness.setHand(player1, List.of(kami));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kami);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersAndPutsOnlyOneLandFromAMixedHandOntoTheBattlefield() {
        KamiOfBambooGroves otherKami = new KamiOfBambooGroves();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new KamiOfBambooGroves(), otherKami, firstForest, secondForest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherKami, firstForest);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getCard().getId()).isEqualTo(secondForest.getId());
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
