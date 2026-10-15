package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.Rewind;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.m.MindSculpt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraAvatar.class, Rewind.class, MindRot.class, MindSculpt.class})
class SerraAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Serra Avatar's power and toughness equal the controller's life total")
    void ptEqualsControllerLife() {
        harness.setLife(player1, 17);
        Permanent avatar = addCreatureReady(player1, new SerraAvatar());

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(17);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(17);
    }

    @Test
    @DisplayName("Serra Avatar's power and toughness update when life total changes")
    void ptUpdatesWithLife() {
        harness.setLife(player1, 20);
        Permanent avatar = addCreatureReady(player1, new SerraAvatar());

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(20);

        harness.setLife(player1, 12);
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(12);
    }

    @Test
    @DisplayName("When Serra Avatar is put into the graveyard, a trigger shuffles it into its owner's library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player1, new java.util.ArrayList<>());
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new SerraAvatar());
        harness.setLife(player1, 5);
        avatar.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Serra Avatar");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Serra Avatar");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Avatar"));
    }

    @Test
    @DisplayName("When Serra Avatar is countered from the stack, its trigger shuffles it into its owner's library")
    void counteredSpellTriggersFromStack() {
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of());

        SerraAvatar avatar = new SerraAvatar();
        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, avatar, "{4}{W}{W}{W}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Avatar");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Serra Avatar");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(avatar.getId()));
    }

    @Test
    @DisplayName("Milling Serra Avatar puts it in the graveyard before its trigger returns it")
    void milledAvatarShufflesIntoLibrary() {
        SerraAvatar avatar = new SerraAvatar();
        harness.setLibrary(player2, List.of(avatar));
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Serra Avatar");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Serra Avatar");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(avatar);
    }

    @Test
    @DisplayName("Discarding Serra Avatar triggers its return to its owner's library")
    void discardedAvatarShufflesIntoLibrary() {
        SerraAvatar avatar = new SerraAvatar();
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(avatar));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Serra Avatar");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Serra Avatar");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(avatar);
    }

    @Test
    @DisplayName("Serra Avatar's characteristic ability also applies in its owner's hand")
    void ptInHandUsesOwnerLife() {
        SerraAvatar avatar = new SerraAvatar();
        harness.setHand(player2, List.of(avatar));
        harness.setLife(player1, 20);
        harness.setLife(player2, 7);

        assertThat(gqs.getEffectiveCardPower(gd, avatar)).isEqualTo(7);
        assertThat(gqs.getEffectiveCardToughness(gd, avatar)).isEqualTo(7);

        harness.setLife(player2, 13);
        assertThat(gqs.getEffectiveCardPower(gd, avatar)).isEqualTo(13);
        assertThat(gqs.getEffectiveCardToughness(gd, avatar)).isEqualTo(13);
    }
}
