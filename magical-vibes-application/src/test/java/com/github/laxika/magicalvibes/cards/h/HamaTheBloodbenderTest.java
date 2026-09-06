package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HamaTheBloodbender.class, Divination.class, GrizzlyBears.class, Swamp.class})
class HamaTheBloodbenderTest extends BaseCardTest {

    @Test
    void millsAndMayExileAnEligibleCardFromTheTargetPlayersGraveyard() {
        Card eligible = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Swamp();
        Card firstMilled = new GrizzlyBears();
        Card secondMilled = new GrizzlyBears();
        Card thirdMilled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(eligible, creature, land));
        harness.setLibrary(player2, List.of(firstMilled, secondMilled, thirdMilled));
        harness.setHand(player1, List.of(new HamaTheBloodbender()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        Permanent hama = findPermanent(player1, "Hama, the Bloodbender");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(5)
                .contains(creature, land)
                .contains(firstMilled, secondMilled, thirdMilled)
                .doesNotContain(eligible);
        assertThat(gd.getCardsExiledByPermanent(hama.getId())).containsExactly(eligible);
    }

    @Test
    void castsATrackedCardByWaterbendingItsManaValue() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Permanent firstContributor = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondContributor = addCreatureReady(player1, new GrizzlyBears());
        Permanent thirdContributor = addCreatureReady(player1, new GrizzlyBears());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromExileWithWaterbend(player1, exiledCard.getId(),
                List.of(firstContributor.getId(), secondContributor.getId(), thirdContributor.getId()));
        harness.passBothPriorities();

        assertThat(firstContributor.isTapped()).isTrue();
        assertThat(secondContributor.isTapped()).isTrue();
        assertThat(thirdContributor.isTapped()).isTrue();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledCard);
    }

    @Test
    void losesThePermissionWhenHamaLeavesTheBattlefield() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hama);

        assertThatThrownBy(() -> harness.castFromExileWithWaterbend(player1, exiledCard.getId(), List.of()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void cannotPayTheNormalManaCostInsteadOfWaterbending() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }
}
