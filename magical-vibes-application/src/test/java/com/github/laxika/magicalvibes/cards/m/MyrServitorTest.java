package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyrServitorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger returns all Myr Servitors from each player's graveyard")
    void returnsAllMyrServitorsFromEachGraveyard() {
        MyrServitor source = new MyrServitor();
        MyrServitor player1Servitor = new MyrServitor();
        MyrServitor player2Servitor = new MyrServitor();
        Card unrelated = new GrizzlyBears();

        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(player1Servitor, unrelated));
        harness.setGraveyard(player2, List.of(player2Servitor));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(player1Servitor.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(player2Servitor.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(unrelated.getId());
    }

    @Test
    @DisplayName("Trigger does nothing if its source leaves the battlefield before resolution")
    void doesNothingIfSourceLeavesBeforeResolution() {
        MyrServitor source = new MyrServitor();
        MyrServitor graveyardServitor = new MyrServitor();

        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(graveyardServitor));

        advanceToUpkeep(player1);
        Permanent sourcePermanent = findPermanent(player1, "Myr Servitor");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sourcePermanent));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(graveyardServitor.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(graveyardServitor.getId());
    }
}
