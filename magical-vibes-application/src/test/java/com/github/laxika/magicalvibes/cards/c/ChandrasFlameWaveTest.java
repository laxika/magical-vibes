package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandrasFlameWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the target player and each creature they control")
    void damagesTargetPlayerAndTheirCreatures() {
        harness.setLife(player2, 20);
        Permanent targetSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent targetGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(targetSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(targetGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(ownGiant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Finds Chandra, Flame's Fury in the graveyard and puts it into hand")
    void findsNamedCardInGraveyard() {
        Card chandraFlamesFury = namedCard("Chandra, Flame's Fury");
        harness.setGraveyard(player1, List.of(chandraFlamesFury));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chandra, Flame's Fury");
        harness.assertNotInGraveyard(player1, "Chandra, Flame's Fury");
    }

    @Test
    @DisplayName("Finds Chandra, Flame's Fury in the library and puts it into hand")
    void findsNamedCardInLibrary() {
        harness.setLibrary(player1, List.of(namedCard("Chandra, Flame's Fury")));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Chandra, Flame's Fury");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
