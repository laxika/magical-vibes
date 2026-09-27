package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TifaMartialArtist.class, Gigantosaurus.class, GrizzlyBears.class})
class TifaMartialArtistTest extends BaseCardTest {

    @Test
    @DisplayName("High-power combat damage untaps your creatures and grants an extra combat in the first combat")
    void highPowerCombatDamageUntapsCreaturesAndGrantsExtraCombat() {
        addReady(player1, new TifaMartialArtist());
        Permanent attacker = addReady(player1, new Gigantosaurus());
        Permanent tappedCreature = addReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        gd.combatPhasesThisTurn = 1;
        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage from a creature below seven power does not trigger")
    void lowPowerCombatDamageDoesNotTrigger() {
        addReady(player1, new TifaMartialArtist());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("High-power combat damage in a later combat untaps creatures without granting another combat")
    void laterCombatDoesNotGrantAnotherCombat() {
        addReady(player1, new TifaMartialArtist());
        Permanent attacker = addReady(player1, new Gigantosaurus());
        Permanent tappedCreature = addReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        gd.combatPhasesThisTurn = 2;
        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
