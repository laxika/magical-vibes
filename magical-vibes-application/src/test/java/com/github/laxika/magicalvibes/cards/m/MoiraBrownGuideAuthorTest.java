package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoiraBrownGuideAuthor.class, GrizzlyBears.class, Forest.class})
class MoiraBrownGuideAuthorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wasteland Survival Guide Equipment token")
    void createsGuideToken() {
        harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();

        Permanent guide = findPermanent(player1, "Wasteland Survival Guide");
        assertThat(guide.getCard().isToken()).isTrue();
        assertThat(guide.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(guide.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.BOOK, CardSubtype.EQUIPMENT);
    }

    @Test
    @DisplayName("Attacking puts a quest counter on a target nonland permanent you control")
    void attackingPutsQuestCounterOnTarget() {
        harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.enterBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(land.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent moira = findPermanent(player1, "Moira Brown, Guide Author");
        harness.handlePermanentChosen(player1, moira.getId());
        harness.passBothPriorities();

        assertThat(moira.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Guide boosts its equipped creature for quest counters you control")
    void guideScalesWithControlledQuestCounters() {
        harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();
        Permanent guide = findPermanent(player1, "Wasteland Survival Guide");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide),
                0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(guide.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        Permanent moira = findPermanent(player1, "Moira Brown, Guide Author");
        harness.handlePermanentChosen(player1, moira.getId());
        harness.passBothPriorities();

        assertThat(moira.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple attackers trigger once and can put the counter on the Guide")
    void multipleAttackersPutOnlyOneCounterOnGuide() {
        harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();
        Permanent guide = findPermanent(player1, "Wasteland Survival Guide");
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(guide.getId()).doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, guide.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(guide.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(findPermanent(player1, "Moira Brown, Guide Author").getCounterCount(CounterType.QUEST))
                .isZero();
    }

    @Test
    @DisplayName("The Guide counts all your quest counters, including on lands, and updates continuously")
    void guideCountsCountersAcrossControlledPermanents() {
        Permanent moira = harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();
        Permanent guide = findPermanent(player1, "Wasteland Survival Guide");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.enterBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.enterBattlefieldAndReturn(player2, new Forest());
        moira.setCounterCount(CounterType.QUEST, 2);
        guide.setCounterCount(CounterType.QUEST, 3);
        land.setCounterCount(CounterType.QUEST, 4);
        land.setCounterCount(CounterType.CHARGE, 5);
        opponentLand.setCounterCount(CounterType.QUEST, 10);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide),
                0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);

        land.setCounterCount(CounterType.QUEST, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("An opponent attacking does not trigger Moira")
    void opponentAttackDoesNotPutQuestCounter() {
        Permanent moira = harness.enterBattlefieldAndReturn(player1, new MoiraBrownGuideAuthor());
        resolveAllTriggers();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(moira.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(findPermanent(player1, "Wasteland Survival Guide").getCounterCount(CounterType.QUEST))
                .isZero();
    }
}
