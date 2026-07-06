package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MikaeusTheUnhallowedTest extends BaseCardTest {

    

    @Test
    @DisplayName("Other non-Human creatures you control get +1/+1 and undying")
    void otherNonHumanCreaturesGetBoostAndUndying() {
        Permanent mikaeus = harness.addToBattlefieldAndReturn(player1, new MikaeusTheUnhallowed());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.UNDYING)).isTrue();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.UNDYING)).isFalse();

        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.UNDYING)).isFalse();

        assertThat(gqs.getEffectivePower(gd, mikaeus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mikaeus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Granted undying returns another non-Human creature with a +1/+1 counter")
    void grantedUndyingReturnsNonHumanCreature() {
        harness.addToBattlefield(player1, new MikaeusTheUnhallowed());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Walking Corpse"));
        resolveUntilStackEmpty();

        Permanent returned = findPermanentByName(player1, "Walking Corpse");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("Human that deals combat damage to Mikaeus's controller is destroyed")
    void humanDamageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new MikaeusTheUnhallowed());
        Permanent attacker = addReadyCreature(player1, new EliteVanguard());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
    }

    @Test
    @DisplayName("Non-Human that deals combat damage to Mikaeus's controller is not destroyed")
    void nonHumanDamageSourceIsNotDestroyed() {
        harness.addToBattlefield(player2, new MikaeusTheUnhallowed());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    private void resolveUntilStackEmpty() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
