package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskOfGriselbrand.class, CandlegroveWitch.class, InfernalGrasp.class, Forest.class})
class MaskOfGriselbrandTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying and lifelink")
    void equippedCreatureHasFlyingAndLifelink() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("When the equipped creature dies, paying its power in life draws that many cards")
    void payingPowerDrawsThatManyCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    @DisplayName("Declining the death trigger neither pays life nor draws cards")
    void decliningDeathTriggerDoesNothing() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void equipMovesKeywordsToTheNewCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        mask.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void insufficientLifeDoesNotPayOrDraw() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void equipmentControllerPaysAndDrawsWhenOpponentsCreatureDies() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void deathOfUnequippedCreatureDoesNotTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new MaskOfGriselbrand());
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -3})
    void nonpositivePowerPaysZeroAndDrawsNothing(int powerModifier) {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        creature.setPowerModifier(powerModifier);
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfGriselbrand());
        mask.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killEquippedCreature(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void killEquippedCreature(Permanent creature) {
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }
}
