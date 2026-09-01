package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsotericDuplicator.class, ChromaticStar.class})
class EsotericDuplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("A sacrificed artifact can be copied at the next end step")
    void copiesAnotherSacrificedArtifactAtNextEndStep() {
        addReady(player1, new EsotericDuplicator());
        Permanent star = addReady(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, star.getId());
        resolveUntilInteractionOrEmpty();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "RED");
            resolveUntilInteractionOrEmpty();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy.class))
                .hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Sacrificing Esoteric Duplicator copies the sacrificed artifact itself")
    void copiesItselfWhenSacrificed() {
        Permanent duplicator = addReady(player1, new EsotericDuplicator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, duplicator.getId());
        resolveUntilInteractionOrEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveUntilInteractionOrEmpty() {
        while (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
