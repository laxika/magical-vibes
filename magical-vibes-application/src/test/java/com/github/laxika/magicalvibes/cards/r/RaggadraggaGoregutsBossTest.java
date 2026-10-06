package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientBronzeDragon;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NikyaOfTheOldWays;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaggadraggaGoregutsBoss.class, ElvishMystic.class, GrizzlyBears.class, ScaledWurm.class,
        AncientBronzeDragon.class, NikyaOfTheOldWays.class, ParadiseMantle.class})
class RaggadraggaGoregutsBossTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled creatures with mana abilities get +2/+2")
    void boostsControlledManaCreatures() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMystic = addCreatureReady(player2, new ElvishMystic());

        assertThat(gqs.getEffectivePower(gd, mystic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystic)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMystic)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMystic)).isEqualTo(1);
    }

    @Test
    @DisplayName("A mana creature that attacks is untapped")
    void attackingManaCreatureIsUntapped() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());

        declareAttackers(List.of(1));
        mystic.tap();
        resolveAllTriggers();

        assertThat(mystic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a spell with seven mana spent untaps, boosts, and gives trample to a target creature")
    void sevenManaSpellTriggersTargetedAbility() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new ScaledWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell with less than seven mana spent does not trigger the targeted ability")
    void fewerThanSevenManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures with triggered mana abilities get the anthem bonus")
    void boostsCreaturesWithTriggeredManaAbilities() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent nikya = addCreatureReady(player1, new NikyaOfTheOldWays());

        assertThat(gqs.getEffectivePower(gd, nikya)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, nikya)).isEqualTo(7);
    }

    @Test
    @DisplayName("An attacking creature with a triggered mana ability is untapped")
    void untapsAttackerWithTriggeredManaAbility() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent nikya = addCreatureReady(player1, new NikyaOfTheOldWays());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(nikya.isTapped()).isTrue();
            resolveAllTriggers();
        });

        assertThat(nikya.isTapped()).isFalse();
        assertThat(nikya.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Raggadragga gets its own anthem bonus when it gains a mana ability")
    void boostsItselfWhenGrantedManaAbility() {
        Permanent boss = addCreatureReady(player1, new RaggadraggaGoregutsBoss());
        harness.addToBattlefield(player1, new ParadiseMantle());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, boss.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boss)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, boss)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature granted a mana ability gets the bonus and untaps when attacking")
    void grantedManaAbilityQualifiesForBothAbilities() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ParadiseMantle());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Only attacking mana creatures are untapped")
    void untapsEachManaAttackerButNotOtherCreatures() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent firstMystic = addCreatureReady(player1, new ElvishMystic());
        Permanent secondMystic = addCreatureReady(player1, new ElvishMystic());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattackingMystic = addCreatureReady(player1, new ElvishMystic());
        nonattackingMystic.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2, 3));
            resolveAllTriggers();
        });

        assertThat(firstMystic.isTapped()).isFalse();
        assertThat(secondMystic.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(nonattackingMystic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opposing mana creatures are not untapped when they attack")
    void doesNotUntapOpposingManaAttacker() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent opponentMystic = addCreatureReady(player2, new ElvishMystic());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(opponentMystic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exactly seven mana spent can boost an untapped opposing creature until end of turn")
    void exactlySevenManaCanBoostUntappedOpponentAndExpires() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncientBronzeDragon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent spending seven mana on a spell does not trigger Raggadragga")
    void opponentExpensiveSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AncientBronzeDragon()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

}
