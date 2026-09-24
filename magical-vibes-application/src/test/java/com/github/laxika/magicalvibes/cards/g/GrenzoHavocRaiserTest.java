package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrenzoHavocRaiser.class, Divination.class, GrizzlyBears.class})
class GrenzoHavocRaiserTest extends BaseCardTest {

    private static final String GOAD_MODE = "Goad target creature that player controls";
    private static final String EXILE_MODE = "Exile the top card of that player's library. Until end of turn, you may cast that card "
            + "and you may spend mana as though it were mana of any color to cast that spell.";

    @Test
    @DisplayName("Goad mode lets the controller choose a creature controlled by the damaged player")
    void goadsCreatureControlledByDamagedPlayer() {
        Permanent grenzo = addCreatureReady(player1, new GrenzoHavocRaiser());
        grenzo.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, GOAD_MODE);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exile mode grants an any-color cast permission until end of turn")
    void exilesTopCardAndAllowsAnyColorCast() {
        Permanent grenzo = addCreatureReady(player1, new GrenzoHavocRaiser());
        grenzo.setAttacking(true);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, EXILE_MODE);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast + 2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }
}
