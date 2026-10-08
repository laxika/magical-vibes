package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeliodGodOfTheSun;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YunaHopeOfSpira.class, GrizzlyBears.class, HeliodGodOfTheSun.class,
        Insight.class, Shock.class, Disenchant.class, Pacifism.class, ProdigalSorcerer.class,
        EnchantedEvening.class})
class YunaHopeOfSpiraTest extends BaseCardTest {

    @Test
    void grantsKeywordsToYunaAndEnchantmentCreaturesOnlyDuringYourTurn() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent heliod = harness.addToBattlefieldAndReturn(player1, new HeliodGodOfTheSun());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(heliod), 0,
                null, null);
        harness.passBothPriorities();

        Permanent cleric = findPermanent(player1, "Cleric");
        assertThat(gqs.hasKeyword(gd, yuna, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, yuna, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cleric, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, yuna, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void wardIsActiveOnlyDuringYourTurn() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, yuna.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void doesNotHaveWardDuringAnOpponentsTurn() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, yuna.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void returnsAnEnchantmentFromTheGraveyardWithAFinalityCounterAtYourEndStep() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Insight");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void opponentCanPayWardToResolveTheirSpell() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, yuna.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(yuna.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardCountersAnOpponentsActivatedAbility() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, yuna.getId());
        harness.passBothPriorities();

        assertThat(yuna.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantsWardToEnchantmentCreatureTokens() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        harness.addToBattlefield(player1, new HeliodGodOfTheSun());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        Permanent cleric = findPermanent(player1, "Cleric");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, cleric.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Cleric");
        assertThat(cleric.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void canChooseNoEnchantmentToReturn() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Insight");
        harness.assertNotOnBattlefield(player1, "Insight");
    }

    @Test
    void doesNotReturnEnchantmentsDuringAnOpponentsEndStep() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        harness.setGraveyard(player1, List.of(new Insight()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Insight");
    }

    @Test
    void returnedNoncreatureEnchantmentIsExiledWhenDestroyed() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Insight");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Insight");
        harness.assertNotInGraveyard(player1, "Insight");
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(insight);
    }

    @Test
    void returnedAuraEntersWithAFinalityCounter() {
        harness.addToBattlefield(player1, new YunaHopeOfSpira());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent returned = findPermanent(player1, "Pacifism");
        assertThat(returned.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void yunaHasOnlyOneWardAbilityWhenSheIsAlsoAnEnchantment() {
        Permanent yuna = harness.addToBattlefieldAndReturn(player1, new YunaHopeOfSpira());
        harness.addToBattlefield(player1, new EnchantedEvening());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, yuna.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(yuna.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
