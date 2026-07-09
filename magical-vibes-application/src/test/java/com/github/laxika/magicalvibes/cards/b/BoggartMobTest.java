package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoggartMobTest extends BaseCardTest {

    private Permanent addReadyBoggartMob() {
        Permanent perm = new Permanent(new BoggartMob());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyGoblin() {
        Permanent perm = new Permanent(new SkirkProspector());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyNonGoblin() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → triggers onto stack
        harness.passBothPriorities(); // resolve the ally-combat-damage trigger (MayEffect prompt)
    }

    private long goblinRogueTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Rogue"))
                .count();
    }

    @Test
    @DisplayName("Accepting the may ability creates a 1/1 black Goblin Rogue token")
    void goblinCombatDamageCreatesToken() {
        addReadyBoggartMob();
        Permanent goblin = addReadyGoblin();
        goblin.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Rogue"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Declining the may ability creates no token")
    void decliningCreatesNoToken() {
        addReadyBoggartMob();
        Permanent goblin = addReadyGoblin();
        goblin.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(goblinRogueTokens()).isZero();
    }

    @Test
    @DisplayName("A non-Goblin dealing combat damage does not trigger Boggart Mob")
    void nonGoblinDoesNotTrigger() {
        addReadyBoggartMob();
        Permanent bears = addReadyNonGoblin();
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        // Player2 took combat damage but Boggart Mob's ability never fired.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(goblinRogueTokens()).isZero();
    }

    @Test
    @DisplayName("Boggart Mob triggers for itself when it deals combat damage")
    void triggersForItself() {
        Permanent mob = addReadyBoggartMob();
        mob.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(goblinRogueTokens()).isEqualTo(1);
    }
}
