package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SouthWindAvatar.class, GrizzlyBears.class, Murder.class})
class SouthWindAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to an allied creature's toughness and drains each opponent")
    void gainsLifeFromAllyCreatureDeathAndDrainsOpponents() {
        harness.addToBattlefield(player1, new SouthWindAvatar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        destroy(player1, player1, "Grizzly Bears");

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerForOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new SouthWindAvatar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        destroy(player1, player2, "Grizzly Bears");

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new SouthWindAvatar());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        destroy(player1, player1, "South Wind Avatar");

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster,
                         com.github.laxika.magicalvibes.model.Player targetController,
                         String targetName) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, harness.getPermanentId(targetController, targetName));
        resolveAllTriggers();
    }
}
