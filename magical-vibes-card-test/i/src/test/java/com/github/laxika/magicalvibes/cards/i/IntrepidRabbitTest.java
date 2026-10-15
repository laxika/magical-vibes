package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntrepidRabbit.class, BraveKinDuo.class, Savor.class})
class IntrepidRabbitTest extends BaseCardTest {

    @Test
    void canBoostItselfAndBoostExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new IntrepidRabbit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent rabbit = findPermanent(player1, "Intrepid Rabbit");
        harness.handlePermanentChosen(player1, rabbit.getId());
        harness.passBothPriorities();

        assertThat(rabbit.getEffectivePower()).isEqualTo(4);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(3);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(rabbit.getEffectivePower()).isEqualTo(3);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void offspringStillCreatesCopyWhenBoostTargetDies() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        harness.setHand(player1, List.of(new IntrepidRabbit()));
        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0, duo.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, duo.getId());
        harness.assertNotOnBattlefield(player1, "Brave-Kin Duo");
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, findPermanent(player1, "Intrepid Rabbit").getId());
            harness.passBothPriorities();
        }
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Intrepid Rabbit"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void entersAndBoostsTargetCreatureYouControl() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        harness.setHand(player1, List.of(new IntrepidRabbit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(duo.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(duo.getEffectivePower()).isEqualTo(2);
        assertThat(duo.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        harness.setHand(player1, List.of(new IntrepidRabbit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0, duo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, duo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
        Permanent currentDuo = findPermanent(player1, "Brave-Kin Duo");
        assertThat(currentDuo.getEffectivePower()).isEqualTo(3);
        assertThat(currentDuo.getEffectiveToughness()).isEqualTo(3);
    }
}
