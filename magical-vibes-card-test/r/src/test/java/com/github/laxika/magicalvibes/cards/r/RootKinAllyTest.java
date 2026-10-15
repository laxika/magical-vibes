package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootKinAlly.class, BorosRecruit.class})
class RootKinAllyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping itself and another creature gives Root-Kin Ally +2/+2")
    void tappingItselfAndAnotherCreatureBoostsIt() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        Permanent creature = addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
        assertThat(ally.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses which two creatures to tap")
    void choosesTwoCreaturesToTap() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        Permanent first = addCreatureReady(player1, new BorosRecruit());
        Permanent second = addCreatureReady(player1, new BorosRecruit());
        Permanent spare = addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(spare.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only untapped creatures you control are offered as payment choices")
    void onlyUntappedCreaturesYouControlCanBeTapped() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        Permanent first = addCreatureReady(player1, new BorosRecruit());
        Permanent second = addCreatureReady(player1, new BorosRecruit());
        Permanent tapped = addCreatureReady(player1, new BorosRecruit());
        tapped.tap();
        Permanent opponent = addCreatureReady(player2, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ally.getId(), first.getId(), second.getId())
                .doesNotContain(tapped.getId(), opponent.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(ally.isTapped()).isFalse();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(tapped.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability requires two untapped creatures you control")
    void requiresTwoUntappedCreatures() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ally), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Convoke pays both green and generic mana with summoning-sick creatures")
    void convokePaysEntireCostWithCreatures() {
        Permanent firstGreen = harness.addToBattlefieldAndReturn(player1, new RootKinAlly());
        Permanent secondGreen = harness.addToBattlefieldAndReturn(player1, new RootKinAlly());
        Permanent firstRecruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent secondRecruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent thirdRecruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent fourthRecruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        List<Permanent> contributors = List.of(firstGreen, secondGreen, firstRecruit,
                secondRecruit, thirdRecruit, fourthRecruit);
        harness.setHand(player1, List.of(new RootKinAlly()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                contributors.stream().map(Permanent::getId).toList());

        assertThat(contributors).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Root-Kin Ally")).isEqualTo(3);
        harness.assertNotInHand(player1, "Root-Kin Ally");
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the ability's tap cost")
    void summoningSickCreaturesCanPayCost() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new RootKinAlly());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);

        assertThat(ally.isTapped()).isTrue();
        assertThat(recruit.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
    }

    @Test
    @DisplayName("A tapped Root-Kin Ally can activate again and the boosts accumulate")
    void tappedAllyCanActivateAgain() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        addCreatureReady(player1, new BorosRecruit());
        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();

        assertThat(ally.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(7);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
