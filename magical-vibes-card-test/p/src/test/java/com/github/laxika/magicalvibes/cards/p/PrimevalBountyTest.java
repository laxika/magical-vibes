package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimevalBounty.class, GrizzlyBears.class, Spellbook.class, Forest.class})
class PrimevalBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell creates a 3/3 Beast token")
    void creatureSpellCreatesBeastToken() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the trigger

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getEffectivePower()).isEqualTo(3);
        assertThat(beast.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a noncreature spell puts three +1/+1 counters on a creature you control")
    void noncreatureSpellPutsCounters() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The counter trigger cannot target a creature an opponent controls")
    void counterTriggerCannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature spell does not trigger the counter ability")
    void creatureSpellDoesNotPutCounters() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Landfall — playing a land gains 3 life")
    void landfallGainsThreeLife() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }
    @Test
    @DisplayName("A creature entering without being cast does not create a Beast")
    void creatureEnteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new PrimevalBounty());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Beast")).isZero();
    }

    @Test
    @DisplayName("Opponent spells do not trigger either spell-cast ability")
    void opponentSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Spellbook()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isZero();
        assertThat(countPermanents(player2, "Beast")).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's land does not gain life for either player")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A land entering without being played triggers each Bounty")
    void landEnteringWithoutPlayTriggersEachBounty() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        harness.addToBattlefield(player1, new PrimevalBounty());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("The Beast trigger resolves before the creature and survives its source leaving")
    void beastTriggerSurvivesSourceLeaving() {
        Permanent bounty = harness.addToBattlefieldAndReturn(player1, new PrimevalBounty());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(bounty);
        gd.playerGraveyards.get(player1.getId()).add(bounty.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The counter trigger fails if its creature is no longer controlled by you")
    void counterTriggerRechecksController() {
        harness.addToBattlefield(player1, new PrimevalBounty());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Beast")).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
    }
}
