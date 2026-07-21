package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HullbreakerHorrorTest extends BaseCardTest {

    // Flash + can't be countered + whenever you cast a spell, choose up to one bounce.

    @Test
    @DisplayName("This spell can't be countered")
    void cannotBeCountered() {
        HullbreakerHorror horror = new HullbreakerHorror();
        harness.setHand(player1, List.of(horror));
        harness.addMana(player1, ManaColor.BLUE, 7);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, horror.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hullbreaker Horror"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Permanent mode returns target nonland permanent to its owner's hand")
    void permanentModeBouncesNonland() {
        harness.addToBattlefield(player1, new HullbreakerHorror());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0);
        // Trigger on top of Opt — resolve trigger into mode prompt
        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.PERMANENT);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.getStackResolutionService().resolveTopOfStack(gd); // bounce effect

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Spell mode returns target spell you don't control to its owner's hand")
    void spellModeBouncesOpponentSpell() {
        harness.addToBattlefield(player1, new HullbreakerHorror());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        // Stack: bears, Opt, Hullbreaker trigger (top)
        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.SPELL);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.getStackResolutionService().resolveTopOfStack(gd); // bounce spell

        assertThat(gd.stack.stream().noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"))).isTrue();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing do nothing leaves the board unchanged")
    void noneModeDoesNothing() {
        harness.addToBattlefield(player1, new HullbreakerHorror());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0);
        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.NONE);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
