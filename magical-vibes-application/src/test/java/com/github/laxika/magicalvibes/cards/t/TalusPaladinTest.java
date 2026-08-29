package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TalusPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry independently offers lifelink and a +1/+1 counter")
    void ownAllyEntryOffersBothChoices() {
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalusPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Talus Paladin");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.LIFELINK)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Declining lifelink does not prevent accepting the counter choice")
    void choicesAreIndependent() {
        harness.setHand(player1, List.of(new TalusPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent paladin = findPermanent(player1, "Talus Paladin");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isFalse();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Allies gain lifelink while non-Allies do not")
    void grantsLifelinkOnlyToAllies() {
        harness.setHand(player1, List.of(new JoinTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> allies = findPermanents(player1, "Soldier Ally");
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalusPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(allies).allSatisfy(ally -> assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue());
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Talus Paladin"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.LIFELINK)).isFalse();
        harness.handleMayAbilityChosen(player1, false);
    }
}
