package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reincarnation.class, GoForTheThroat.class, GrizzlyBears.class, GiantSpider.class,
        Shock.class, Spellbook.class})
class ReincarnationTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput() && guard++ < 12) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Lets its controller choose a creature from the targeted creature owner's graveyard")
    void choosesCreatureFromTargetOwnersGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card spider = new GiantSpider();
        spider.setOwnerId(player2.getId());
        Card shock = new Shock();
        shock.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(spider, shock));

        harness.setHand(player1, List.of(new Reincarnation(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        resolveStack();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).contains(spider).doesNotContain(shock).hasSize(2);

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(spider));
        resolveStack();

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Returns the targeted creature when it is the only creature in its owner's graveyard")
    void returnsTargetWhenItIsOnlyCreatureInGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card shock = new Shock();
        shock.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(shock));

        harness.setHand(player1, List.of(new Reincarnation(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        resolveStack();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new Reincarnation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }
}
