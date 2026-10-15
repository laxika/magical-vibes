package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenedictionOfMoons.class, GhorClanSavage.class, Mortify.class})
class BenedictionOfMoonsTest extends BaseCardTest {

    @Test
    void gainsLifeAndHauntsTargetCreature() {
        harness.setLife(player1, 10);
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GhorClanSavage()).getId();
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Benediction of Moons"));

        destroyWithMortify(creatureId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void doesNotHauntWhenThereIsNoCreatureToTarget() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Benediction of Moons");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotExileOrGainMoreLifeWhenHauntTargetDiesBeforeResolution() {
        harness.setLife(player1, 10);
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GhorClanSavage()).getId();
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, creatureId);

        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghor-Clan Savage");
        harness.assertInGraveyard(player1, "Benediction of Moons");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    void canHauntOwnCreatureAndLeavesCardExiledAfterDeathTrigger() {
        harness.setLife(player1, 10);
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new GhorClanSavage()).getId();
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        destroyWithMortify(creatureId);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ghor-Clan Savage");
        harness.assertNotInGraveyard(player1, "Benediction of Moons");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Benediction of Moons"));
    }

    private void destroyWithMortify(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
