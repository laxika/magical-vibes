package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrummingStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the controller's spells ripple 4")
    void givesControllerSpellsRipple() {
        prepareSpellCast(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not give an opponent's spells ripple")
    void doesNotGiveOpponentsSpellsRipple() {
        prepareSpellCast(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private void prepareSpellCast(Player caster) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(caster);
        harness.addToBattlefield(player1, new ThrummingStone());
        gd.playerDecks.get(caster.getId()).clear();
        gd.playerDecks.get(caster.getId()).add(new GrizzlyBears());
        harness.setHand(caster, List.of(new GrizzlyBears()));
        harness.addMana(caster, ManaColor.GREEN, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
    }
}
