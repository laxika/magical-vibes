package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
import com.github.laxika.magicalvibes.cards.z.ZodiacMonkey;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreetSavvy.class, Forest.class, GrizzlyBears.class, ZodiacMonkey.class, RimeDryad.class})
class StreetSavvyTest extends BaseCardTest {

    @Test
    @DisplayName("Street Savvy gives the enchanted creature +0/+2")
    void enchantedCreatureGetsToughnessBoost() {
        Permanent creature = readyCreature(player2, new GrizzlyBears());
        attachStreetSavvy(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature can block a forestwalking creature through a Forest")
    void enchantedCreatureCanBlockForestwalker() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);

        beginBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can block a snow-landwalking creature through a snow Forest")
    void enchantedCreatureCanBlockSnowLandwalker() {
        Permanent snowForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        TestCards.mutableCard(snowForest).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        Permanent attacker = readyCreature(player1, new RimeDryad());
        attacker.setAttacking(true);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        attachStreetSavvy(blocker);

        beginBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Street Savvy does not let an unenchanted creature block a forestwalking creature")
    void unenchantedCreatureCannotBlockForestwalker() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The landwalk permission ends when Street Savvy leaves the battlefield")
    void permissionEndsWhenAuraLeavesBattlefield() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = readyAttacker(player1);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent aura = attachStreetSavvy(blocker);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent readyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent readyAttacker(Player player) {
        Permanent attacker = readyCreature(player, new ZodiacMonkey());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent attachStreetSavvy(Permanent creature) {
        Permanent aura = new Permanent(new StreetSavvy());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        return aura;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
