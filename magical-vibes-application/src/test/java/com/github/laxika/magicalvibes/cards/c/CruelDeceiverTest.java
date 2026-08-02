package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CruelDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        addReadyDeceiver(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Revealing a land makes damage from Cruel Deceiver destroy the damaged creature")
    void landRevealDestroysDamagedCreature() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);

        // The 2/4 Spider survives 2 damage, so only the granted ability can kill it.
        Permanent spider = addReadySpider(player2);
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // combat damage
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(spider);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Revealing a nonland card leaves the damaged creature alive")
    void nonlandRevealGrantsNothing() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent spider = addReadySpider(player2);
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // combat damage
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    @DisplayName("Without the ability granted, damage from Cruel Deceiver does not destroy the creature")
    void withoutActivationNothingIsDestroyed() {
        addReadyDeceiver(player1);
        Permanent spider = addReadySpider(player2);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // combat damage
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDeceiver(Player player) {
        Permanent deceiver = new Permanent(new CruelDeceiver());
        deceiver.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(deceiver);
        return deceiver;
    }

    private Permanent addReadySpider(Player player) {
        return addCreatureReady(player, new GiantSpider());
    }
}
