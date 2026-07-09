package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HellsCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature during upkeep returns target creature card from graveyard to the battlefield")
    void reanimatesTargetCreatureDuringUpkeep() {
        addReady(player1, new HellsCaretaker());
        Permanent fodder = addReady(player1, new GrizzlyBears());

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        enterUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        // Sacrificed creature is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Reanimated creature is on the battlefield, no longer in the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate outside the controller's upkeep")
    void cannotActivateOutsideUpkeep() {
        addReady(player1, new HellsCaretaker());
        addReady(player1, new GrizzlyBears());

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        addReady(player1, new HellsCaretaker());
        addReady(player1, new GrizzlyBears());

        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));

        enterUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void enterUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
