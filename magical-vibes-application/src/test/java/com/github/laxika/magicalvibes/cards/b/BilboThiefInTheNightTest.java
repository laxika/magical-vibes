package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BilboThiefInTheNight.class, CounselOfTheSoratami.class, GrizzlyBears.class, MindStone.class})
class BilboThiefInTheNightTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a sorcery from the graveyard for one less and exiles it")
    void castsSorceryFromGraveyardForReducedCostAndExilesIt() {
        addReadyBilbo();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Casts an artifact from the graveyard without exiling it")
    void castsArtifactFromGraveyardWithoutExilingIt() {
        addReadyBilbo();
        MindStone mindStone = new MindStone();
        harness.setGraveyard(player1, List.of(mindStone));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(mindStone.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(mindStone.getId()));
    }

    @Test
    @DisplayName("Offers only artifacts, instants, and sorceries from the graveyard")
    void filtersGraveyardChoicesToAllowedSpellTypes() {
        addReadyBilbo();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        MindStone mindStone = new MindStone();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(counsel, mindStone, creature));

        declareAttack();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(counsel, mindStone);

        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(counsel, mindStone, creature);
    }

    private void addReadyBilbo() {
        addCreatureReady(player1, new BilboThiefInTheNight());
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }
}
