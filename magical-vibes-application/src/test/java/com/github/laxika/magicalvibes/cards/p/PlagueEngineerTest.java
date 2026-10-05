package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GoblinWelder;
import com.github.laxika.magicalvibes.cards.w.WeatherseedElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueEngineer.class, AvianChangeling.class, GoblinWelder.class, WeatherseedElf.class})
class PlagueEngineerTest extends BaseCardTest {

    private Permanent addPlague(CardSubtype chosen) {
        Permanent plague = harness.addToBattlefieldAndReturn(player1, new PlagueEngineer());
        plague.setChosenSubtype(chosen);
        return plague;
    }

    @Test
    @DisplayName("Choosing a creature type on enter stores the chosen type")
    void choosesTypeOnEnter() {
        harness.setHand(player1, List.of(new PlagueEngineer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Plague Engineer").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures of the chosen type an opponent controls get -1/-1")
    void weakensOpponentCreaturesOfChosenType() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Creatures of the chosen type the controller controls are not affected")
    void doesNotWeakenOwnCreatures() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures of a different type are not affected")
    void doesNotAffectOtherTypes() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new WeatherseedElf());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, elf);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature with the chosen type among multiple types is affected")
    void matchesAnyCreatureSubtype() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        addPlague(CardSubtype.ARTIFICER);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The -1/-1 disappears when Plague Engineer leaves the battlefield")
    void effectRemovedWhenPlagueLeaves() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        Permanent plague = addPlague(CardSubtype.GOBLIN);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(-1);

        gd.playerBattlefields.get(player1.getId()).remove(plague);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
    }

    @Test
    void choosingTypeKillsOpposingOneToughnessCreature() {
        harness.addToBattlefield(player2, new GoblinWelder());
        harness.addToBattlefield(player1, new GoblinWelder());
        harness.setHand(player1, List.of(new PlagueEngineer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertNotOnBattlefield(player2, "Goblin Welder");
        harness.assertInGraveyard(player2, "Goblin Welder");
        harness.assertOnBattlefield(player1, "Goblin Welder");
    }

    @Test
    void affectsChangelingAndStacksAcrossDifferentChosenTypes() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        addPlague(CardSubtype.GOBLIN);

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);

        addPlague(CardSubtype.ELF);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Avian Changeling");
        harness.assertInGraveyard(player2, "Avian Changeling");
    }

    @Test
    void separateEngineersKeepIndependentChoices() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new WeatherseedElf());
        addPlague(CardSubtype.GOBLIN);
        addPlague(CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isEqualTo(-1);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(-1);
    }

    @Test
    void affectsCreaturesEnteringAfterTypeWasChosen() {
        addPlague(CardSubtype.GOBLIN);

        harness.enterBattlefieldAndReturn(player2, new GoblinWelder());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Goblin Welder");
        harness.assertInGraveyard(player2, "Goblin Welder");
    }

    @Test
    void deathtouchDestroysBlockerWithMoreToughnessThanDamage() {
        Permanent attacker = addPlague(CardSubtype.GOBLIN);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PlagueEngineer());
        blocker.setChosenSubtype(CardSubtype.ELF);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Plague Engineer");
        harness.assertInGraveyard(player2, "Plague Engineer");
    }
}
