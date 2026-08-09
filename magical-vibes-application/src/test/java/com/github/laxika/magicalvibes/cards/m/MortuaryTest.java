package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MortuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature that dies on top of its controller's library")
    void putsDyingCreatureOnTopOfLibrary() {
        Card forest = new Forest();
        harness.setLibrary(player1, new ArrayList<>(List.of(forest)));
        harness.addToBattlefield(player1, new Mortuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears.getCard(), forest);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent is put into the graveyard")
    void doesNotTriggerForNoncreaturePermanent() {
        Card forest = new Forest();
        harness.setLibrary(player1, new ArrayList<>(List.of(forest)));
        harness.addToBattlefield(player1, new Mortuary());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spellbook.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the dying creature leaves the graveyard before resolution")
    void fizzlesIfDyingCreatureLeavesGraveyard() {
        harness.setLibrary(player1, new ArrayList<>());
        harness.addToBattlefield(player1, new Mortuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        Card deadCard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(bears.getCard().getId()))
                .findFirst()
                .orElseThrow();
        gd.playerGraveyards.get(player1.getId()).remove(deadCard);
        gd.addToExile(player1.getId(), deadCard);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
