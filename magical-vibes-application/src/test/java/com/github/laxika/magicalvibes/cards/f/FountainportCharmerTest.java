package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FountainportCharmer.class, RoostOfDrakes.class})
class FountainportCharmerTest extends BaseCardTest {

    @Test
    void creatureCardsInHandPerpetuallyCostOneLess() {
        harness.setHand(player1, List.of(new FountainportCharmer(), new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getEffectivePower() == 1
                        && permanent.getEffectiveToughness() == 1);
        assertThat(countPermanents(player1, "Fountainport Charmer")).isEqualTo(2);
    }

    @Test
    void unpaidOffspringDoesNotCreateToken() {
        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fountainport Charmer")).isEqualTo(1);
        assertThat(findPermanent(player1, "Fountainport Charmer").getCard().isToken()).isFalse();
    }

    @Test
    void offspringTriggerAddsASecondReductionThatCanPayForOffspring() {
        harness.setHand(player1, List.of(new FountainportCharmer(), new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fountainport Charmer")).isEqualTo(4);
    }

    @Test
    void creatureAddedToHandAfterResolutionDoesNotReceiveReduction() {
        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionPersistsAfterCreatureLeavesBattlefield() {
        harness.setHand(player1, List.of(new FountainportCharmer(), new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        var reducedCharmer = findPermanents(player1, "Fountainport Charmer").get(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, reducedCharmer));

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fountainport Charmer")).isEqualTo(2);
    }

    @Test
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Drake")).isZero();
        assertThat(countPermanents(player1, "Fountainport Charmer")).isEqualTo(2);
    }

    @Test
    void reductionDoesNotAffectOpponentsHand() {
        harness.setHand(player1, List.of(new FountainportCharmer()));
        harness.setHand(player2, List.of(new FountainportCharmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.ensurePriority(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
