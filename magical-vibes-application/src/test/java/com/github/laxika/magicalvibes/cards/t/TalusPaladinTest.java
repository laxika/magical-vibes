package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TalusPaladin.class, JoinTheRanks.class, WalkingAtlas.class, Conspiracy.class, RayOfCommand.class})
class TalusPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry independently offers lifelink and a +1/+1 counter")
    void ownAllyEntryOffersBothChoices() {
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
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
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
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
        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();

        List<Permanent> allies = findPermanents(player1, "Soldier Ally");
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(allies).allSatisfy(ally -> assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue());
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Talus Paladin"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.LIFELINK)).isFalse();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(findPermanent(player1, "Talus Paladin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Ally token entering produces its own optional ability")
    void anotherAllyEntryTriggersForEachToken() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new TalusPaladin());
        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier Ally")).hasSize(2)
                .allSatisfy(ally -> assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Ally entering does not trigger the ability")
    void nonAllyEntryDoesNotTrigger() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new TalusPaladin());
        harness.castFromHand(player1, new WalkingAtlas(), "{2}");
        resolveAllTriggers();

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing Allies neither trigger the ability nor receive lifelink")
    void opposingAlliesAreExcluded() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new TalusPaladin());
        harness.castFromHand(player2, new JoinTheRanks(), "{3}{W}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isTrue();
        assertThat(findPermanents(player2, "Soldier Ally")).hasSize(2)
                .allSatisfy(ally -> assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isFalse());
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Both optional choices may be declined")
    void bothChoicesMayBeDeclined() {
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        Permanent paladin = findPermanent(player1, "Talus Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Its own entry triggers even when Conspiracy replaces its Ally subtype")
    void ownEntryTriggersWithoutAllySubtype() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        Permanent paladin = findPermanent(player1, "Talus Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Paladin stolen before its ability resolves does not receive lifelink")
    void stolenSourceDoesNotReceiveLifelink() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new TalusPaladin());
        harness.castFromHand(player1, new TalusPaladin(), "{3}{W}");
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, entering.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(entering);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.LIFELINK)).isFalse();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        assertThat(gd.stack).isEmpty();
    }
}
