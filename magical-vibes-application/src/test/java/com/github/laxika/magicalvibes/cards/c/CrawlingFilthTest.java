package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHonoredDead;
import com.github.laxika.magicalvibes.cards.k.KamiOfTatteredShoji;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        CrawlingFilth.class,
        KamiOfFalseHope.class,
        KamiOfTheHonoredDead.class,
        KamiOfTatteredShoji.class,
        TorrentOfStone.class
})
class CrawlingFilthTest extends BaseCardTest {

    /** Deals lethal damage to Crawling Filth, firing its soulshift trigger. */
    private void torrentOfStoneToKillFilth() {
        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Crawling Filth"));
    }

    @Test
    @DisplayName("Soulshift 5 returns a targeted Spirit with mana value 5 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        torrentOfStoneToKillFilth();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of False Hope");
        harness.assertNotInGraveyard(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Spirits with mana value 6 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        Card cheapSpirit = new KamiOfTatteredShoji();
        Card expensiveSpirit = new KamiOfTheHonoredDead();
        Card opponentSpirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        torrentOfStoneToKillFilth();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 5 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        harness.setGraveyard(player1, List.of(new KamiOfTheHonoredDead()));

        torrentOfStoneToKillFilth();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift can be declined at resolution even when a legal Spirit target exists")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        torrentOfStoneToKillFilth();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();

        // The target is chosen as the triggered ability is put on the stack; the "you may"
        // choice is made when that ability resolves.
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kami of False Hope");
        harness.assertNotInHand(player1, "Kami of False Hope");
    }

    @Test
    void soulshiftRequiresTargetBeforeResolutionChoice() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        torrentOfStoneToKillFilth();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Kami of False Hope");
    }

    @Test
    void soulshiftReturnsSpiritAtManaValueLimit() {
        harness.addToBattlefield(player1, new CrawlingFilth());
        Card spirit = new KamiOfTatteredShoji();
        harness.setGraveyard(player1, List.of(spirit, new TorrentOfStone()));

        torrentOfStoneToKillFilth();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(spirit.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Kami of Tattered Shoji");
        harness.assertNotInGraveyard(player1, "Kami of Tattered Shoji");
        harness.assertInGraveyard(player1, "Torrent of Stone");
        harness.assertInGraveyard(player1, "Crawling Filth");
    }

    @Test
    void fearRejectsWhiteBlocker() {
        addCreatureReady(player1, new CrawlingFilth());
        addCreatureReady(player2, new KamiOfFalseHope());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fearAllowsBlackBlocker() {
        addCreatureReady(player1, new CrawlingFilth());
        var blocker = addCreatureReady(player2, new CrawlingFilth());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
