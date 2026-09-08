package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger sacrifices Dreamcatcher and draws a card for an Arcane spell")
    void acceptingArcaneTriggerSacrificesAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the trigger keeps Dreamcatcher and draws no card")
    void decliningTriggerKeepsDreamcatcherAndDoesNotDraw() {
        Permanent dreamcatcher = addDreamcatcher();
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInGraveyard(player1, "Dreamcatcher");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Dreamcatcher")
    void unrelatedSpellDoesNotTrigger() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addDreamcatcher() {
        return harness.addToBattlefieldAndReturn(player1, new Dreamcatcher());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
