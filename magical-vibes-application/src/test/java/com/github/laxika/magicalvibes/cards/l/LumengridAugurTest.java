package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.b.Blossombind;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumengridAugur.class, Ornithopter.class, Annul.class, Blossombind.class})
class LumengridAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws, discards an artifact, and untaps Lumengrid Augur")
    void artifactDiscardUntapsAugur() {
        Permanent augur = addCreatureReady(player1, new LumengridAugur());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setLibrary(player2, List.of(new Annul()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(augur.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInHand(player2, "Annul");
    }

    @Test
    @DisplayName("A can't-become-untapped effect prevents the conditional untap")
    void cantBecomeUntappedPreventsConditionalUntap() {
        Permanent augur = addCreatureReady(player1, new LumengridAugur());
        Permanent blossomsbind = new Permanent(new Blossombind());
        blossomsbind.setAttachedTo(augur.getId());
        gd.playerBattlefields.get(player1.getId()).add(blossomsbind);
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setLibrary(player2, List.of(new Annul()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(augur.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInHand(player2, "Annul");
    }

    @Test
    @DisplayName("Discarding a nonartifact card does not untap Lumengrid Augur")
    void nonartifactDiscardDoesNotUntapAugur() {
        Permanent augur = addCreatureReady(player1, new LumengridAugur());
        harness.setHand(player2, List.of(new Annul()));
        harness.setLibrary(player2, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(augur.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Annul");
        harness.assertInHand(player2, "Ornithopter");
    }

    @Test
    @DisplayName("The controller may be targeted and discard the drawn artifact")
    void controllerCanBeTargetedAndDiscardDrawnArtifact() {
        Permanent augur = addCreatureReady(player1, new LumengridAugur());
        harness.setHand(player1, List.of(new Annul()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);

        assertThat(augur.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInHand(player1, "Annul");
    }
}
