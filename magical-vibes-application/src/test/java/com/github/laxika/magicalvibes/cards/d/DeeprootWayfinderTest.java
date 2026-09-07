package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootWayfinder.class, Forest.class, GrizzlyBears.class})
class DeeprootWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage surveils one, then returns a land card tapped")
    void combatDamageSurveilsThenReturnsLandTapped() {
        Card topCard = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(land, new GrizzlyBears()));
        addWayfinderAttacking();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining the land return leaves the graveyard unchanged")
    void declinesLandReturn() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addWayfinderAttacking();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    private void addWayfinderAttacking() {
        addCreatureReady(player1, new DeeprootWayfinder());
        declareAttackers(List.of(0));
    }
}
