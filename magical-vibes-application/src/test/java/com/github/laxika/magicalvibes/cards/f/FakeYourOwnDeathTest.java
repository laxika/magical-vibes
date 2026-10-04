package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FakeYourOwnDeath.class, GrizzlyBears.class, DoomBlade.class})
class FakeYourOwnDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+0")
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature returns tapped and creates a Treasure when it dies")
    void returnsTappedAndCreatesTreasureOnDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().isToken()).isTrue();
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("An opponent's creature returns under its owner's control and its controller creates the Treasure")
    void returnsUnderOwnerControlAndCreatesTreasureForCreatureController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The granted death trigger wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Returning creates a new creature without the boost or granted death ability")
    void returnedCreatureDoesNotKeepSpellEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);

        destroy(player2, returned);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("An older death trigger cannot return a creature that returned and died again")
    void olderTriggerCannotReturnNewGraveyardObject() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var creatureCard = creature.getCard();

        castOn(creature);
        castOn(creature);
        destroy(player2, creature);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        destroy(player2, returned);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FakeYourOwnDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
