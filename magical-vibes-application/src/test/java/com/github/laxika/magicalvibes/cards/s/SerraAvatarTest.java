package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.Rewind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraAvatar.class, Rewind.class})
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
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Avatar");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Serra Avatar");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(avatar.getId()));
    }
}
