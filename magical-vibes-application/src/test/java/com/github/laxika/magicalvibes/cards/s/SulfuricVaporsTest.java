package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.PitFight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SulfuricVapors.class, Shock.class, SerraAngel.class, ConsumeSpirit.class,
        ProdigalPyromancer.class, PitFight.class, GrizzlyBears.class, HillGiant.class})
class SulfuricVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell deals one extra damage to a player regardless of its controller")
    void redSpellDealsExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A red spell deals one extra damage to a permanent")
    void redSpellDealsExtraDamageToPermanent() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        Permanent target = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonred spell is not boosted")
    void nonredSpellIsNotBoosted() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.setHand(player2, List.of(new ConsumeSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 2, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A red activated ability is not boosted")
    void redActivatedAbilityIsNotBoosted() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Multiple Sulfuric Vapors each add one damage")
    void multipleSulfuricVaporsStack() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Combat damage from a red creature is not boosted")
    void redCombatDamageIsNotBoosted() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A red fight spell does not boost damage dealt by the fighting creatures")
    void redFightSpellDoesNotBoostCreatureDamage() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new PitFight()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        UUID player2CreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID player1CreatureId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveInstant(player2, 0, List.of(player2CreatureId, player1CreatureId));

        harness.assertOnBattlefield(player1, "Hill Giant");
        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }
}
