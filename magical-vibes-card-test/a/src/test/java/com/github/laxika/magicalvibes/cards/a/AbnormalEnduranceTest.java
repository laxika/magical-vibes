package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbnormalEndurance.class, GreenwoodSentinel.class, Murder.class})
class AbnormalEnduranceTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AbnormalEndurance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void murder(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    @Test
    @DisplayName("Target creature gets +2/+0")
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to the battlefield tapped when it dies")
    void returnsTappedOnDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castOn(creature);
        murder(player2, creature);
        harness.passBothPriorities(); // resolve the granted death trigger

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature returns under its owner's control")
    void returnsUnderOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castOn(creature);
        murder(player1, creature);
        harness.passBothPriorities(); // resolve the granted death trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The granted death trigger wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        murder(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Returned creature loses the boost and does not return a second time")
    void returnedCreatureIsANewObject() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castOn(creature);
        murder(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);

        murder(player2, returned);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Boost and granted ability remain during the end step")
    void remainsActiveDuringEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castOn(creature);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()) && p.isTapped());
    }
}
