package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.cards.r.RiftBolt;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChameleonBlur.class, AshcoatBear.class, FledglingMawcor.class, RiftBolt.class, SuddenDeath.class})
class ChameleonBlurTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures to players")
    void preventsCombatDamageFromCreaturesToPlayers() {
        harness.setLife(player1, 20);
        addAttacker(player2, new AshcoatBear());

        castChameleonBlur();
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents noncombat damage from creatures to players")
    void preventsNoncombatDamageFromCreaturesToPlayers() {
        harness.setLife(player1, 20);
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());

        castChameleonBlur();
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mawcor), null,
                player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents creature damage to the other player as well")
    void preventsCreatureDamageToOtherPlayer() {
        harness.setLife(player2, 20);
        Permanent mawcor = addCreatureReady(player1, new FledglingMawcor());

        castChameleonBlur();
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mawcor), null,
                player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Stops preventing creature damage after the turn ends")
    void expiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());

        castChameleonBlur();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mawcor), null,
                player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventDamageFromNoncreatureSources() {
        harness.setLife(player1, 20);
        castChameleonBlur();

        harness.setHand(player2, List.of(new RiftBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not prevent creature damage to creatures")
    void doesNotPreventCreatureDamageToCreatures() {
        Permanent attacker = addAttacker(player1, new AshcoatBear());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castChameleonBlur();
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private void castChameleonBlur() {
        harness.castFromHand(player1, new ChameleonBlur(), "{3}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents damage from creatures entering after resolution and from repeated damage events")
    void preventsDamageFromLaterCreaturesAndRepeatedEvents() {
        castChameleonBlur();
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mawcor), null,
                player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        Permanent secondMawcor = addCreatureReady(player2, new FledglingMawcor());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(secondMawcor), null,
                player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents damage from a creature ability after its source leaves the battlefield")
    void preventsDamageAfterSourceLeavesBattlefield() {
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());
        castChameleonBlur();
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mawcor), null,
                player1.getId());

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, mawcor.getId());
        harness.assertInGraveyard(player2, "Fledgling Mawcor");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
