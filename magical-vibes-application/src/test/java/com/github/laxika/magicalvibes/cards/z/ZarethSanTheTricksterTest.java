package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZarethSanTheTrickster.class, ZulaportDuelist.class, GrizzlyBears.class, Forest.class})
class ZarethSanTheTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("The hand ability returns an unblocked attacking Rogue and enters tapped and attacking")
    void abilityUsesAnUnblockedRogue() {
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ZarethSanTheTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, rogue.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Zulaport Duelist");
        Permanent zareth = findPermanent(player1, "Zareth San, the Trickster");
        assertThat(zareth.isTapped()).isTrue();
        assertThat(zareth.isAttacking()).isTrue();
        assertThat(zareth.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The hand ability cannot return a non-Rogue attacker")
    void abilityRequiresARogue() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ZarethSanTheTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage can put any permanent card from the damaged player's graveyard onto the battlefield")
    void combatDamageReanimatesPermanent() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));

        Permanent zareth = addCreatureReady(player1, new ZarethSanTheTrickster());
        zareth.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(forest.getId()));
        harness.assertNotInGraveyard(player2, "Forest");
    }
}
