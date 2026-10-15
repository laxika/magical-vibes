package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PriceOfLoyalty;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorcererClass.class, GrizzlyBears.class, Shock.class, Unsubstantiate.class, PriceOfLoyalty.class})
class SorcererClassTest extends BaseCardTest {

    @Test
    void entersAndDrawsTwoThenDiscardsTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new SorcererClass(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelTwoLetsCreaturesProduceManaForInstantSorcerySpells() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(sorcererClass);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void levelTwoManaCanPayForTheNextClassLevel() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(sorcererClass);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null);
        harness.passBothPriorities();

        assertThat(sorcererClass.getClassLevel()).isEqualTo(3);
    }

    @Test
    void levelThreeDealsIncreasingDamageForEachInstantOrSorceryCastThisTurn() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        levelUpToThree(sorcererClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.passBothPriorities();
    }

    @Test
    void levelThreeCountsSpellsCastInResponseWhenEachTriggerResolves() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        levelUpToThree(sorcererClass);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player2, 12);
    }

    @Test
    void levelThreeStillDealsDamageAfterTriggeringSpellReturnsToHand() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        levelUpToThree(sorcererClass);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Shock");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void levelTwoDoesNotDealCastTriggerDamageButEarlierSpellsCountAfterLevelThree() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        levelUpToTwo(sorcererClass);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);

        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void levelTwoManaCannotPayForAnEnchantment() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(sorcererClass);
        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.setHand(player1, List.of(new SorcererClass()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareForSorcery();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelTwoGrantedTapAbilityRespectsSummoningSickness() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        levelUpToTwo(sorcererClass);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void levelThreeRetainsGrantedManaAbilityAndTriggersForSorceries() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        levelUpToThree(sorcererClass);
        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        harness.handleListChoice(player1, "RED");
        prepareForSorcery();
        harness.setHand(player1, List.of(new PriceOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void cannotSkipLevelTwoOrGainLevelsAtInstantSpeed() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sorcererClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sorcererClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void creatureSpellsNeitherTriggerNorIncreaseLevelThreeDamage() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        levelUpToThree(sorcererClass);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentsSpellsDoNotTriggerLevelThree() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        levelUpToThree(sorcererClass);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void levelUpToTwo(Permanent sorcererClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent sorcererClass) {
        levelUpToTwo(sorcererClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
