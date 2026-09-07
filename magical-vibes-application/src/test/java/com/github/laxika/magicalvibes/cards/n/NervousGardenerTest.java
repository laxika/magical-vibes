package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NervousGardener.class, Forest.class, TempleGarden.class, RakdosGuildgate.class})
class NervousGardenerTest extends BaseCardTest {

    @Test
    void turningFaceUpSearchesForALandWithABasicLandType() {
        Forest forest = new Forest();
        TempleGarden templeGarden = new TempleGarden();
        RakdosGuildgate guildgate = new RakdosGuildgate();
        harness.setLibrary(player1, List.of(forest, templeGarden, guildgate));
        harness.setHand(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent gardener = findPermanent(player1, "Nervous Gardener");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gardener));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, templeGarden);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Temple Garden");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(guildgate, forest);
    }
}
