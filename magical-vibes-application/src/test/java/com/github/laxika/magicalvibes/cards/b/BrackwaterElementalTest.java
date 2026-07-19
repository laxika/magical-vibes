package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Brackwater Elemental")
class BrackwaterElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking schedules a sacrifice that fires at the next end step (survives combat)")
    void attackingSacrificesAtNextEndStep() {
        Permanent elemental = new Permanent(new BrackwaterElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elemental);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger in the postcombat main phase: the sacrifice is delayed to the
        // next end step, so the elemental is still around after combat.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Brackwater Elemental");

        // Reaching the end step sacrifices it.
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brackwater Elemental"));
    }

    @Test
    @DisplayName("Blocking schedules a sacrifice that fires at the next end step")
    void blockingSacrificesAtNextEndStep() {
        Permanent elemental = new Permanent(new BrackwaterElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Brackwater Elemental");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Brackwater Elemental");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Brackwater Elemental"));
    }

    @Test
    @DisplayName("Unearth returns Brackwater Elemental to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Brackwater Elemental"))
                .findFirst().orElseThrow();
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Brackwater Elemental"));
    }

    @Test
    @DisplayName("Unearthed Brackwater Elemental is exiled at the next end step")
    void unearthExiledAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brackwater Elemental"));
    }
}
