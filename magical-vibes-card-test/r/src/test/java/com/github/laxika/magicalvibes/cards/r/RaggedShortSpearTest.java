package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaggedShortSpear.class, Forest.class, Mountain.class, OrdinaryBear.class})
class RaggedShortSpearTest extends BaseCardTest {

    @Test
    void enteringMayDiscardToDrawTwo() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card discarded = new OrdinaryBear();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new RaggedShortSpear(), discarded)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void decliningEnterAbilityDoesNotDiscardOrDraw() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card discarded = new OrdinaryBear();
        harness.setHand(player1, new ArrayList<>(List.of(new RaggedShortSpear(), discarded)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void equippingGivesCreaturePlusTwoPower() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new RaggedShortSpear());
        Permanent creature = addCreatureReady(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void acceptingWithEmptyHandDoesNotDraw() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RaggedShortSpear(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).first().isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void reequippingMovesThePowerBonus() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new RaggedShortSpear());
        Permanent first = addCreatureReady(player1, new OrdinaryBear());
        Permanent second = addCreatureReady(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new RaggedShortSpear());
        Permanent creature = addCreatureReady(player2, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.getAttachedTo()).isNull();
    }

    @Test
    void enteringWithoutBeingCastAllowsChoosingOneCardToDiscard() {
        Card kept = new Forest();
        Card discarded = new OrdinaryBear();
        Card firstDraw = new Mountain();
        Card secondDraw = new Forest();
        Card remaining = new Mountain();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(kept, discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remaining));

        harness.enterBattlefieldAndReturn(player1, new RaggedShortSpear());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void equipCannotBeActivatedWithOnlyTwoMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new RaggedShortSpear());
        Permanent creature = addCreatureReady(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new RaggedShortSpear());
        Permanent creature = addCreatureReady(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.getAttachedTo()).isNull();
    }
}
