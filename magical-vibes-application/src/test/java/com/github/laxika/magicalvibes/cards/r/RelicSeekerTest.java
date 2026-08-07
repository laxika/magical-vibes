package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelicSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming renowned offers the Equipment search, which puts the chosen card into hand")
    void becomingRenownedFindsEquipment() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        setupLibrary();

        attackUnblocked();

        GameData gd = harness.getGameData();
        assertThat(seeker.isRenowned()).isTrue();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.EQUIPMENT));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningSkipsSearch() {
        addCreatureReady(player1, new RelicSeeker());
        setupLibrary();

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An already renowned Relic Seeker does not trigger again")
    void alreadyRenownedDoesNotTrigger() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        seeker.setRenowned(true);
        setupLibrary();

        attackUnblocked();

        GameData gd = harness.getGameData();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked Relic Seeker never becomes renowned, so nothing triggers")
    void blockedDoesNotTrigger() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        addCreatureReady(player2, new WallOfWood());
        setupLibrary();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(seeker.isRenowned()).isFalse();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new ShortSword(), new GrizzlyBears()));
    }
}
