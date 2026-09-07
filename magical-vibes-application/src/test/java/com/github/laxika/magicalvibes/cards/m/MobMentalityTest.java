package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RollingStones;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
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

@CardUsed({MobMentality.class, GrizzlyBears.class, WallOfAir.class, RollingStones.class})
class MobMentalityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has trample")
    void enchantedCreatureHasTrample() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("All non-Wall creatures attacking pumps enchanted creature by attacker count")
    void allNonWallsAttackingPumpsByAttackerCount() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(enchanted.getId());
        addCreatureReady(player1, new GrizzlyBears());

        // Indices: 0 enchanted, 1 aura, 2 other bear — attack with both creatures.
        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(enchanted.getPowerModifier()).isEqualTo(2);
        assertThat(enchanted.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger when a non-Wall creature stays home")
    void doesNotTriggerWhenNonWallStaysHome() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(enchanted.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Mob Mentality"));
        assertThat(enchanted.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Walls that stay home do not prevent the trigger")
    void wallsMayStayHome() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(enchanted.getId());
        addCreatureReady(player1, new WallOfAir());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(enchanted.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking Walls count toward X")
    void attackingWallsCountTowardX() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(enchanted.getId());
        addCreatureReady(player1, new WallOfAir());
        harness.addToBattlefield(player1, new RollingStones());

        // Indices: 0 enchanted, 1 aura, 2 wall, 3 Rolling Stones
        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(enchanted.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +X/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new MobMentality());
        aura.setAttachedTo(enchanted.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(enchanted.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchanted.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can cast Mob Mentality targeting a creature")
    void canCastOntoCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MobMentality()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mob Mentality")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }
}
