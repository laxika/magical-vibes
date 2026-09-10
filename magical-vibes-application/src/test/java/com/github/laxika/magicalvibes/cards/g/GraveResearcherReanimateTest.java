package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GraveResearcherReanimateTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared after surveil leaves three creature cards in the graveyard")
    void becomesPreparedAfterSurveilReachesThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(researcher.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared with fewer than three creature cards in the graveyard")
    void doesNotBecomePreparedBelowThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Casting the prepared Reanimate copy unprepares Grave Researcher and reanimates a creature")
    void castingPreparedCopyUnpreparesAndReanimates() {
        Permanent researcher = addResearcher();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears(), new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID copyId = researcher.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(researcher.isPrepared()).isFalse();
        assertThat(researcher.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertLife(player1, 18);
    }

    private Permanent addResearcher() {
        GraveResearcherReanimate card = new GraveResearcherReanimate();
        Permanent researcher = new Permanent(card);
        researcher.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(researcher);
        return researcher;
    }
}
