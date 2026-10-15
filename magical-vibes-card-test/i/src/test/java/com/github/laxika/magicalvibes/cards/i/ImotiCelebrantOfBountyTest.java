package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.cards.r.RoamingThrone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImotiCelebrantOfBounty.class, CrawWurm.class, HillGiant.class, LlanowarElves.class,
        MammothSpider.class, RoamingThrone.class})
class ImotiCelebrantOfBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Imoti has cascade when cast")
    void ownCascadeTriggersWhenCast() {
        prepareTurn(player1);
        harness.setLibrary(player1, List.of(new HillGiant()));

        harness.castFromHand(player1, new ImotiCelebrantOfBounty(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Spells with mana value 6 or greater get cascade")
    void highManaValueSpellGetsCascade() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());

        // Craw Wurm has mana value 6 and Imoti has mana value 5. The MV-5 Mammoth Spider must be
        // offered, proving the cascade threshold is taken from the triggering spell.
        MammothSpider hit = new MammothSpider();
        harness.setLibrary(player1, List.of(hit, new LlanowarElves()));

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Mammoth Spider");
    }

    @Test
    @DisplayName("Spells with mana value less than 6 do not get cascade")
    void lowerManaValueSpellDoesNotGetCascade() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.castFromHand(player1, new MammothSpider(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Imoti does not grant cascade to an opponent's spell")
    void opponentSpellDoesNotGetCascade() {
        prepareTurn(player2);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        harness.setLibrary(player2, List.of(new LlanowarElves()));

        harness.castFromHand(player2, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cascade casts the hit without mana before Imoti resolves")
    void castsHitBeforeImotiResolves() {
        prepareTurn(player1);
        HillGiant hit = new HillGiant();
        harness.setLibrary(player1, List.of(hit, new LlanowarElves()));

        harness.castFromHand(player1, new ImotiCelebrantOfBounty(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Imoti, Celebrant of Bounty");
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Imoti, Celebrant of Bounty");
    }

    @Test
    @DisplayName("Cascade skips equal mana value and returns declined cards to the bottom")
    void decliningHitReturnsExiledCardsToBottom() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        CrawWurm equalManaValue = new CrawWurm();
        MammothSpider hit = new MammothSpider();
        LlanowarElves unexiled = new LlanowarElves();
        harness.setLibrary(player1, List.of(equalManaValue, hit, unexiled));

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unexiled);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(equalManaValue, hit);
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Craw Wurm");
        harness.assertNotOnBattlefield(player1, "Mammoth Spider");
    }

    @Test
    @DisplayName("Roaming Throne does not double cascade granted to a spell")
    void throneDoesNotDoubleGrantedCascade() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        Permanent throne = new Permanent(new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.DRUID);
        gd.playerBattlefields.get(player1.getId()).add(throne);
        harness.setLibrary(player1, List.of(new MammothSpider(), new LlanowarElves()));

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");

        assertThat(gd.stack).hasSize(2);
    }

    private void prepareTurn(Player activePlayer) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(activePlayer);
    }
}
