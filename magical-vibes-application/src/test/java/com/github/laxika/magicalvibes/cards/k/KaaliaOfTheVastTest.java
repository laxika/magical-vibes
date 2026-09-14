package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaaliaOfTheVast.class, DragonlordDromoka.class, GrizzlyBears.class})
class KaaliaOfTheVastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts an Angel, Demon, or Dragon from hand onto the battlefield tapped and attacking")
    void putsDragonTappedAndAttacking() {
        harness.setHand(player1, List.of(new DragonlordDromoka()));
        attackWithKaalia();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent dragon = findPermanent(player1, "Dragonlord Dromoka");
        assertThat(dragon).isNotNull();
        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only Angel, Demon, or Dragon creature cards are offered")
    void offersOnlyMatchingCreatures() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new DragonlordDromoka()));
        attackWithKaalia();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the matching card in hand")
    void decliningLeavesCardInHand() {
        harness.setHand(player1, List.of(new DragonlordDromoka()));
        attackWithKaalia();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dragonlord Dromoka");
    }

    private void attackWithKaalia() {
        Permanent kaalia = new Permanent(new KaaliaOfTheVast());
        kaalia.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaalia);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        harness.passBothPriorities();
    }
}
