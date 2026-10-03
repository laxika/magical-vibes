package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.cards.r.RallyTheRanks;
import com.github.laxika.magicalvibes.cards.r.Ravenform;
import com.github.laxika.magicalvibes.cards.r.RunAmok;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathknellBerserker.class, PoisonTheCup.class, RallyTheRanks.class, Ravenform.class, RunAmok.class})
class DeathknellBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 black Zombie Berserker when it dies with power 3 or greater")
    void createsZombieBerserkerWhenItsPowerIsAtLeastThree() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithPoisonTheCup(player2, berserker.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Zombie").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE, CardSubtype.BERSERKER);
    }

    @Test
    @DisplayName("Does not create a token when it dies with power less than 3")
    void doesNotCreateTokenWhenItsPowerIsLessThanThree() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());

        destroyWithPoisonTheCup(player2, berserker.getId());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void usesPowerFromRallyTheRanksImmediatelyBeforeDeath() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());
        harness.setHand(player1, List.of(new RallyTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);

        destroyWithPoisonTheCup(player2, berserker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deathknell Berserker");
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    void usesTemporaryPowerBoostWhenItDies() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());
        berserker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, berserker.getId());

        destroyWithPoisonTheCup(player2, berserker.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    void createsTokenForTheControllerOfTheDyingBerserker() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player2, new DeathknellBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyWithPoisonTheCup(player1, berserker.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Zombie")).hasSize(1);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void exileDoesNotTriggerEvenWhenPowerIsThree() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Ravenform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, berserker.getId());

        harness.assertNotOnBattlefield(player1, "Deathknell Berserker");
        harness.assertNotInGraveyard(player1, "Deathknell Berserker");
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void destroyWithPoisonTheCup(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new PoisonTheCup()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
