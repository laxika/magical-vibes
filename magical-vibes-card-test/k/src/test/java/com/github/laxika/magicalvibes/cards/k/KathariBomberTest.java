package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KathariBomber.class, SerraAngel.class, Terminate.class})
@DisplayName("Kathari Bomber")
class KathariBomberTest extends BaseCardTest {

    private Permanent addReadyBomber() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new KathariBomber());
        perm.setSummoningSick(false);
        return perm;
    }

    // ===== When this creature deals combat damage to a player =====

    @Test
    @DisplayName("Combat damage creates two 1/1 Goblins and sacrifices this creature")
    void combatDamageCreatesGoblinsAndSacrifices() {
        Permanent bomber = addReadyBomber();
        bomber.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Goblin"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                });
        harness.assertNotOnBattlefield(player1, "Kathari Bomber");
        harness.assertInGraveyard(player1, "Kathari Bomber");
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches the player")
    void noTriggerWhenBlocked() {
        Permanent bomber = addReadyBomber();
        bomber.setAttacking(true);
        harness.setLife(player2, 20);

        // 4/4 flying blocker soaks all damage; Kathari Bomber deals no damage to the player.
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Goblin");
    }

    // ===== Unearth {3}{B}{R} =====

    @Test
    @DisplayName("Unearth returns Kathari Bomber to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new KathariBomber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Kathari Bomber");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Kathari Bomber");
    }

    @Test
    @DisplayName("Unearthed Kathari Bomber is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new KathariBomber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kathari Bomber");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Bomber"));
    }

    @Test
    @DisplayName("Unearthed Bomber creates Goblins and is exiled when sacrificed")
    void unearthedBomberCombatTriggerExilesIt() {
        harness.setGraveyard(player1, List.of(new KathariBomber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent bomber = findPermanent(player1, "Kathari Bomber");
        bomber.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Goblin")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Kathari Bomber");
        harness.assertNotInGraveyard(player1, "Kathari Bomber");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Bomber"));
    }

    @Test
    @DisplayName("Combat trigger still creates Goblins after Bomber is destroyed")
    void combatTriggerCreatesTokensAfterSourceLeaves() {
        Permanent bomber = addReadyBomber();
        bomber.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bomber.getId());
        harness.assertInGraveyard(player1, "Kathari Bomber");
        harness.assertNotOnBattlefield(player1, "Goblin");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Goblin")).hasSize(2);
        harness.assertInGraveyard(player1, "Kathari Bomber");
    }
}
