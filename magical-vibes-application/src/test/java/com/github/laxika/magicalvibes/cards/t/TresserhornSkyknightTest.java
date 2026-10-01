package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.k.KarplusanWolverine;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.cards.o.OhranYeti;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TresserhornSkyknight.class, BorealGriffin.class, KrovikanScoundrel.class,
        KarplusanWolverine.class, OhranYeti.class})
class TresserhornSkyknightTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures with first strike")
    void preventsCombatDamageFromFirstStrikeCreatures() {
        Permanent skyknight = addCreatureReady(player2, new TresserhornSkyknight());
        skyknight.setBlocking(true);
        skyknight.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new BorealGriffin());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        attacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Tresserhorn Skyknight");
        assertThat(skyknight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures without first strike")
    void doesNotPreventCombatDamageFromOrdinaryCreatures() {
        Permanent skyknight = addCreatureReady(player2, new TresserhornSkyknight());
        skyknight.setBlocking(true);
        skyknight.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new KrovikanScoundrel());
        attacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Tresserhorn Skyknight");
        assertThat(skyknight.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents noncombat damage from a creature with first strike")
    void preventsNoncombatDamageFromFirstStrikeCreature() {
        Permanent skyknight = addCreatureReady(player2, new TresserhornSkyknight());
        Permanent yeti = addCreatureReady(player1, new OhranYeti());
        Permanent wolverine = addCreatureReady(player1, new KarplusanWolverine());
        wolverine.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        int yetiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(yeti);
        harness.activateAbility(player1, yetiIndex, 0, wolverine.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.handlePermanentChosen(player1, skyknight.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveCombat(player1);

        assertThat(skyknight.getMarkedDamage()).isZero();
    }
}
