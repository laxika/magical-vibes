package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.f.FlashOfInsight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyFate;
import com.github.laxika.magicalvibes.cards.p.PrismaticStrands;
import com.github.laxika.magicalvibes.cards.r.RayOfRevelation;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenFogbringer.class, BattleScreech.class, FlaringPain.class, FlashOfInsight.class, GrizzlyFate.class, PrismaticStrands.class, QuietSpeculation.class, RayOfRevelation.class, SuntailHawk.class})
class QuietSpeculationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only cards with flashback from the target player's library")
    void offersOnlyFlashbackCards() {
        Card battleScreech = new BattleScreech();
        Card avenFogbringer = new AvenFogbringer();
        Card flashOfInsight = new FlashOfInsight();
        harness.setLibrary(player2, List.of(battleScreech, avenFogbringer, flashOfInsight));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(battleScreech, flashOfInsight);
    }

    @Test
    @DisplayName("Puts up to three chosen flashback cards into the target player's graveyard")
    void putsUpToThreeFlashbackCardsIntoTargetGraveyard() {
        Card battleScreech = new BattleScreech();
        Card flashOfInsight = new FlashOfInsight();
        Card grizzlyFate = new GrizzlyFate();
        Card avenFogbringer = new AvenFogbringer();
        harness.setLibrary(player2, List.of(battleScreech, flashOfInsight, grizzlyFate, avenFogbringer));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(battleScreech, flashOfInsight, grizzlyFate);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(battleScreech, flashOfInsight, grizzlyFate);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(avenFogbringer);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Can decline after choosing fewer than three cards")
    void canChooseFewerThanThreeCards() {
        Card battleScreech = new BattleScreech();
        Card flashOfInsight = new FlashOfInsight();
        harness.setLibrary(player2, List.of(battleScreech, flashOfInsight));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(battleScreech);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(flashOfInsight);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Stops after the only available flashback card is chosen")
    void stopsWhenOnlyOneFlashbackCardIsAvailable() {
        Card battleScreech = new BattleScreech();
        Card avenFogbringer = new AvenFogbringer();
        harness.setLibrary(player2, List.of(battleScreech, avenFogbringer));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(battleScreech);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(avenFogbringer);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Resolves without a search when the target library has no flashback cards")
    void noMatchingCardsSkipsSearch() {
        Card avenFogbringer = new AvenFogbringer();
        harness.setLibrary(player2, List.of(avenFogbringer));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(avenFogbringer);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        harness.assertInGraveyard(player1, "Quiet Speculation");
    }

    private void castQuietSpeculation(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new QuietSpeculation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Never puts more than three flashback cards into the target player's graveyard")
    void capsSelectionAtThreeCards() {
        Card battleScreech = new BattleScreech();
        Card rayOfRevelation = new RayOfRevelation();
        Card prismaticStrands = new PrismaticStrands();
        Card flaringPain = new FlaringPain();
        Card suntailHawk = new SuntailHawk();
        harness.setLibrary(player2,
                List.of(battleScreech, rayOfRevelation, prismaticStrands, flaringPain, suntailHawk));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(battleScreech, rayOfRevelation, prismaticStrands);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(flaringPain, suntailHawk);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
