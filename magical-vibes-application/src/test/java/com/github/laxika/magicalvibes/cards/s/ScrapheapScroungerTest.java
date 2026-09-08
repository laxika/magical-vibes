package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrapheapScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("The graveyard ability returns Scrapheap Scrounger by exiling another creature card")
    void graveyardAbilityReturnsSelf() {
        ScrapheapScrounger scrounger = new ScrapheapScrounger();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), scrounger));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scrapheap Scrounger");
        harness.assertNotInGraveyard(player1, "Scrapheap Scrounger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard ability requires another creature card to exile")
    void graveyardAbilityRequiresAnotherCreature() {
        harness.setGraveyard(player1, List.of(new ScrapheapScrounger()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        harness.assertInGraveyard(player1, "Scrapheap Scrounger");
    }

    @Test
    @DisplayName("Scrapheap Scrounger cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent scrounger = new Permanent(new ScrapheapScrounger());
        scrounger.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(scrounger);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
