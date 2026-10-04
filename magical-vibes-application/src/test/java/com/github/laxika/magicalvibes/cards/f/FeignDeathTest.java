package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeignDeath.class, PowerWordKill.class, DireWolfProwler.class})
class FeignDeathTest extends BaseCardTest {

    @Test
    @DisplayName("The creature returns tapped with a +1/+1 counter when it dies")
    void returnsTappedWithCounterOnDeath() {
        Permanent creature = addCreature(player1);
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    @DisplayName("The granted death trigger wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        var creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("An opponent's creature returns under its owner's control")
    void returnsOpponentsCreatureToOwner() {
        Permanent creature = addCreature(player2);
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player1, creature);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Dire Wolf Prowler");
        assertThat(returned.getCard().getId()).isEqualTo(creatureCard.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature does not gain a counter or become tapped before dying")
    void doesNotChangeCreatureBeforeDeath() {
        Permanent creature = addCreature(player1);

        castOn(creature);

        assertThat(findPermanent(player1, "Dire Wolf Prowler")).isSameAs(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The returned creature no longer has the granted ability")
    void doesNotReturnAfterSecondDeath() {
        Permanent creature = addCreature(player1);
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Dire Wolf Prowler");

        destroy(player2, returned);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two granted death triggers return the creature only once with one counter")
    void multipleGrantsReturnOnlyOnce() {
        Permanent creature = addCreature(player1);
        var creatureCard = creature.getCard();

        castOn(creature);
        castOn(creature);
        destroy(player2, creature);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dire Wolf Prowler")).hasSize(1);
        Permanent returned = findPermanent(player1, "Dire Wolf Prowler");
        assertThat(returned.getCard().getId()).isEqualTo(creatureCard.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An older death trigger cannot return the creature after it returns and dies again")
    void olderTriggerCannotReturnNewGraveyardObject() {
        Permanent creature = addCreature(player1);
        var creatureCard = creature.getCard();

        castOn(creature);
        castOn(creature);
        destroy(player2, creature);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent returned = findPermanent(player1, "Dire Wolf Prowler");

        destroy(player2, returned);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new DireWolfProwler());
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FeignDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroy(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new PowerWordKill()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
