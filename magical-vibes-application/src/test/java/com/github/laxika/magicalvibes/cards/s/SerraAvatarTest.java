package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerraAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Serra Avatar's power and toughness equal the controller's life total")
    void ptEqualsControllerLife() {
        harness.setLife(player1, 17);
        Permanent avatar = addAvatarReady(player1);

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(17);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(17);
    }

    @Test
    @DisplayName("Serra Avatar's power and toughness update when life total changes")
    void ptUpdatesWithLife() {
        harness.setLife(player1, 20);
        Permanent avatar = addAvatarReady(player1);

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

    private Permanent addAvatarReady(Player player) {
        SerraAvatar card = new SerraAvatar();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
