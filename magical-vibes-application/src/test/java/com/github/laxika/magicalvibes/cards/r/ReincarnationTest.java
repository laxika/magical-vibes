package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ActiveVolcano;
import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reincarnation.class, ActiveVolcano.class, AzureDrake.class, Karakas.class,
        KoboldsOfKherKeep.class})
class ReincarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Lets its controller choose a creature from the targeted creature owner's graveyard")
    void choosesCreatureFromTargetOwnersGraveyard() {
        Card target = new AzureDrake();
        target.setOwnerId(player2.getId());
        harness.addToBattlefield(player2, target);

        Card returned = new KoboldsOfKherKeep();
        returned.setOwnerId(player2.getId());
        Card nonCreature = new Karakas();
        nonCreature.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(returned, nonCreature));

        harness.setHand(player1, List.of(new Reincarnation(), new ActiveVolcano()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, 0, targetId);
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).contains(returned).doesNotContain(nonCreature).hasSize(2);

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(returned));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
        harness.assertNotInGraveyard(player2, "Kobolds of Kher Keep");
        harness.assertInGraveyard(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Karakas");
    }

    @Test
    @DisplayName("Returns the targeted creature when it is the only creature in its owner's graveyard")
    void returnsTargetWhenItIsOnlyCreatureInGraveyard() {
        Card target = new AzureDrake();
        target.setOwnerId(player2.getId());
        harness.addToBattlefield(player2, target);

        Card nonCreature = new Karakas();
        nonCreature.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(nonCreature));

        harness.setHand(player1, List.of(new Reincarnation(), new ActiveVolcano()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, 0, targetId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Azure Drake");
        harness.assertNotInGraveyard(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Karakas");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Uses the targeted creature's owner's graveyard when another player controls it")
    void usesTargetOwnersGraveyardWhenControllerDiffers() {
        Card target = new AzureDrake();
        target.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, target);

        Card returned = new KoboldsOfKherKeep();
        returned.setOwnerId(player2.getId());
        Card nonCreature = new Karakas();
        nonCreature.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(returned, nonCreature));

        harness.setHand(player2, List.of(new Reincarnation(), new ActiveVolcano()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Azure Drake");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.castInstant(player2, 0, 0, targetId);
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).contains(returned).doesNotContain(nonCreature).hasSize(2);

        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(returned));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
        harness.assertInGraveyard(player2, "Azure Drake");
        harness.assertNotOnBattlefield(player1, "Azure Drake");
    }

    @Test
    @DisplayName("Does not return a creature when the target survives")
    void doesNothingWhenTargetSurvives() {
        Card target = new AzureDrake();
        target.setOwnerId(player2.getId());
        harness.addToBattlefield(player2, target);

        Card returned = new KoboldsOfKherKeep();
        returned.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(returned));
        harness.setHand(player1, List.of(new Reincarnation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Kobolds of Kher Keep");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Karakas());
        harness.setHand(player1, List.of(new Reincarnation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID karakasId = harness.getPermanentId(player2, "Karakas");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, karakasId))
                .isInstanceOf(IllegalStateException.class);
    }
}
