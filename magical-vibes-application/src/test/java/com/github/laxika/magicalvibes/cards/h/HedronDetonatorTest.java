package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedronDetonator.class, Spellbook.class, Forest.class})
class HedronDetonatorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target opponent when an artifact enters under your control")
    void artifactEntryDealsDamageToTargetOpponent() {
        harness.addToBattlefield(player1, new HedronDetonator());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrifices two artifacts and grants play permission for the exiled top card")
    void sacrificesArtifactsToExileTopCardForTheTurn() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent detonator = harness.addToBattlefieldAndReturn(player1, new HedronDetonator());
        detonator.setSummoningSick(false);

        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 2, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(permanent -> permanent.getCard().getName())
                .isEqualTo("Hedron Detonator");
    }
}
