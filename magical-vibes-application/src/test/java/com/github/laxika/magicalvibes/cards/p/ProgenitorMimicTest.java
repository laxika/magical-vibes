package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProgenitorMimicTest extends BaseCardTest {

    private Permanent copyGrizzlyBears(boolean copy) {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProgenitorMimic()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, copy);

        if (copy) {
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.handlePermanentChosen(player1, bearsId);
        }

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Progenitor Mimic"))
                .findFirst().orElse(null);
    }

    private long controlledBears() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
    }

    @Test
    @DisplayName("Enters as a copy of a creature and makes a token copy at the beginning of upkeep")
    void copiesCreatureAndTokensEachUpkeep() {
        Permanent mimic = copyGrizzlyBears(true);
        assertThat(mimic).isNotNull();
        assertThat(mimic.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(controlledBears()).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlledBears()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy it creates is a token, so it never triggers itself")
    void createdCopyIsAToken() {
        assertThat(copyGrizzlyBears(true)).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .filter(p -> p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenPlayerDeclines() {
        assertThat(copyGrizzlyBears(false)).isNull();
        harness.assertInGraveyard(player1, "Progenitor Mimic");
    }
}
