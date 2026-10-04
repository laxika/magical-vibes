package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.cards.c.CenterSoul;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusoryGains.class, SpidersilkNet.class, CenterSoul.class, ColossodonYearling.class})
class IllusoryGainsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Illusory Gains steals the enchanted creature")
    void resolvingStealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new ColossodonYearling());
        Permanent gains = castAndResolve(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gains.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Attaches to and steals each opponent creature that enters")
    void attachesToEnteringOpponentCreature() {
        Permanent original = addCreatureReady(player2, new ColossodonYearling());
        Permanent gains = castAndResolve(original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ColossodonYearling()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Colossodon Yearling"))
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gains.getAttachedTo()).isEqualTo(entered.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(entered.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not reattach when a creature the controller controls enters")
    void doesNotReattachToOwnCreature() {
        Permanent original = addCreatureReady(player2, new ColossodonYearling());
        Permanent gains = castAndResolve(original);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ColossodonYearling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gains.getAttachedTo()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new ColossodonYearling());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());

        harness.setHand(player1, List.of(new IllusoryGains()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void cannotMoveOntoCreatureWithProtectionFromBlue() {
        Permanent original = addCreatureReady(player2, new ColossodonYearling());
        Permanent gains = castAndResolve(original);
        Permanent entered = harness.enterBattlefieldAndReturn(player2, new ColossodonYearling());

        harness.setHand(player2, List.of(new CenterSoul()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, entered.getId());
        harness.handleListChoice(player2, "BLUE");
        harness.passBothPriorities();

        assertThat(gains.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gains, original);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(entered);
    }

    @Test
    void eachReattachmentReturnsPreviouslyEnchantedCreature() {
        Permanent original = addCreatureReady(player2, new ColossodonYearling());
        Permanent gains = castAndResolve(original);

        Permanent first = harness.enterBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.passBothPriorities();
        assertThat(gains.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original);

        Permanent second = harness.enterBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.passBothPriorities();
        assertThat(gains.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gains, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original, first);
    }

    @Test
    void canInitiallyEnchantOwnCreatureAndThenStealOpponentCreature() {
        Permanent original = addCreatureReady(player1, new ColossodonYearling());
        Permanent gains = castAndResolve(original);
        Permanent entered = harness.enterBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.passBothPriorities();

        assertThat(gains.getAttachedTo()).isEqualTo(entered.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gains, original, entered);
    }

    private Permanent castAndResolve(Permanent creature) {
        harness.setHand(player1, List.of(new IllusoryGains()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Illusory Gains");
    }
}
