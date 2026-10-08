package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
import com.github.laxika.magicalvibes.cards.z.ZodiacMonkey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreetSavvy.class, Forest.class, SnowCoveredForest.class, GrizzlyBears.class,
        ZodiacMonkey.class, RimeDryad.class, MistralCharger.class})
class StreetSavvyTest extends BaseCardTest {

    @Test
    @DisplayName("Street Savvy gives the enchanted creature +0/+2")
    void enchantedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature can block a forestwalking creature through a Forest")
    void enchantedCreatureCanBlockForestwalker() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can block a snow-landwalking creature through a snow Forest")
    void enchantedCreatureCanBlockSnowLandwalker() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        Permanent attacker = addCreatureReady(player1, new RimeDryad());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Street Savvy does not let an unenchanted creature block a forestwalking creature")
    void unenchantedCreatureCannotBlockForestwalker() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Street Savvy only grants the landwalk permission to its enchanted creature")
    void permissionIsLimitedToEnchantedCreature() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent enchantedBlocker = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(enchantedBlocker);
        Permanent unenchantedBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(unenchantedBlocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The landwalk permission ends when Street Savvy leaves the battlefield")
    void permissionEndsWhenAuraLeavesBattlefield() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachStreetSavvy(blocker);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Street Savvy can be cast on an opponent's creature and attaches on resolution")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StreetSavvy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Street Savvy");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        prepareDeclareBlockers();
        declareBlock(creature, attacker);

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Street Savvy does not allow a tapped creature to block through landwalk")
    void tappedEnchantedCreatureCannotBlock() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);
        blocker.tap();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Street Savvy does not let the enchanted creature ignore flying")
    void enchantedCreatureCannotBlockFlyer() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent readyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new ZodiacMonkey());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent attachStreetSavvy(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StreetSavvy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
