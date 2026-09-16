package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallenShinobi.class, Forest.class, GrizzlyBears.class})
class FallenShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top two cards of the damaged player's library")
    void combatDamageExilesTopTwoCards() {
        addAttackingShinobi();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card remainder = new Forest();
        harness.setLibrary(player2, List.of(first, second, remainder));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainder);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("The exiled land and creature can be played without paying their mana costs")
    void playsExiledLandAndCreatureForFree() {
        addAttackingShinobi();
        Forest exiledLand = new Forest();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledLand, exiledCreature));

        resolveCombatAndTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, exiledLand.getId());
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledLand.getId())).isNull();
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNull();
    }

    @Test
    @DisplayName("Ninjutsu puts Fallen Shinobi onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FallenShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent shinobi = findPermanent(player1, "Fallen Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }

    private Permanent addAttackingShinobi() {
        Permanent shinobi = addCreatureReady(player1, new FallenShinobi());
        shinobi.setAttacking(true);
        shinobi.setAttackTarget(player2.getId());
        return shinobi;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
