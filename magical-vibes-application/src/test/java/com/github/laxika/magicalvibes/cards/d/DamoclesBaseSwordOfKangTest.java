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

@CardUsed({DamoclesBaseSwordOfKang.class, GrizzlyBears.class, Forest.class})
class DamoclesBaseSwordOfKangTest extends BaseCardTest {

    private static final String SACRIFICE = "Sacrifice a nontoken creature";
    private static final String LIFE_AND_DRAW = "Lose 2 life and draw two cards";

    @Test
    @DisplayName("The damaged player chooses between sacrificing and losing life to draw")
    void damagedPlayerChoosesVillainousChoice() {
        Permanent damocles = addDamocles();
        addCreatureReady(player2, new GrizzlyBears());
        damocles.setAttacking(true);

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(SACRIFICE, LIFE_AND_DRAW);
    }

    @Test
    @DisplayName("Sacrificing a nontoken creature prevents the life loss and draw")
    void sacrificeBranch() {
        Permanent damocles = addDamocles();
        addCreatureReady(player2, new GrizzlyBears());
        damocles.setAttacking(true);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, SACRIFICE);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The life branch makes the damaged player lose life and draws two cards")
    void lifeAndDrawBranch() {
        Permanent damocles = addDamocles();
        damocles.setAttacking(true);
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, LIFE_AND_DRAW);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(library.stream().map(Card::getId).toList());
    }

    private Permanent addDamocles() {
        Permanent damocles = new Permanent(new DamoclesBaseSwordOfKang());
        damocles.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(damocles);
        return damocles;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
