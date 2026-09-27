package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeskitTheFleshSculptor.class, GrizzlyBears.class, Spellbook.class})
class KeskitTheFleshSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices three other artifacts or creatures and puts two looked-at cards into hand")
    void sacrificesThreeAndSplitsTopThree() {
        Permanent keskit = addReadyKeskit();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Card first = new GrizzlyBears();
        Card chosen = new Spellbook();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(first, chosen, third));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.allCards()).containsExactly(first, chosen, third);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), chosen.getId()));

        assertThat(keskit.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstCreature.getCard(), artifact.getCard(), secondCreature.getCard(), third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(keskit);
    }

    @Test
    @DisplayName("Cannot sacrifice Keskit itself as one of the three permanents")
    void sourceIsExcludedFromSacrificeCost() {
        addReadyKeskit();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private Permanent addReadyKeskit() {
        Permanent keskit = harness.addToBattlefieldAndReturn(player1, new KeskitTheFleshSculptor());
        keskit.setSummoningSick(false);
        return keskit;
    }
}
