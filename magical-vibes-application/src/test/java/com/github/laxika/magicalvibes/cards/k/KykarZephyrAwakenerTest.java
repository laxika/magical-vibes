package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KykarZephyrAwakenerTest extends BaseCardTest {

    private static final String FLICKER =
            "Exile another target creature you control. Return that card at the beginning of the next end step";
    private static final String TOKEN = "Create a 1/1 white Spirit creature token with flying";

    @Test
    @DisplayName("Choosing the token mode creates a flying Spirit")
    void createsSpiritToken() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        castNoncreatureSpell();

        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing the flicker mode returns another creature at the next end step")
    void flickersAnotherCreatureUntilNextEndStep() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent kykar = findPermanent(player1, "Kykar, Zephyr Awakener");

        castNoncreatureSpell();
        harness.handleListChoice(player1, FLICKER);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId())
                .doesNotContain(kykar.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Kykar")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castNoncreatureSpell() {
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }
}
