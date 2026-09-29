package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeReaper.class, PathToExile.class})
class TimeReaperTest extends BaseCardTest {

    @Test
    void putsDamagedPlayersExiledCardOnBottomAndGainsLife() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, exiledCard.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        List<com.github.laxika.magicalvibes.model.Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(library.size() - 1)).isSameAs(exiledCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotTargetACardNotOwnedByTheDamagedPlayer() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player1, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
