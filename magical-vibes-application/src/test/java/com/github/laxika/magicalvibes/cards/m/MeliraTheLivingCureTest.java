package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeliraTheLivingCureTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces the first poison event with one counter and stops later poison events that turn")
    void limitsPoisonCountersForTheTurn() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        harness.setHand(player1, List.of(new IchorRats(), new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a targeted creature when it is put into a graveyard this turn")
    void returnsTargetedCreature() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveStack();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a targeted artifact when it is put into a graveyard this turn")
    void returnsTargetedArtifact() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.activateAbility(player1, 0, null, spellbook.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, spellbook.getId());
        resolveStack();

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target Melira itself")
    void cannotTargetSelf() {
        Permanent melira = harness.addToBattlefieldAndReturn(player1, new MeliraTheLivingCure());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, melira.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact");
        harness.assertOnBattlefield(player1, "Melira, the Living Cure");
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
