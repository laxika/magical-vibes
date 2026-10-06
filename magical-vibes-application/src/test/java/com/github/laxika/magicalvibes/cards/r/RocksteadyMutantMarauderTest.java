package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BebopSkullCrossbones;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({RocksteadyMutantMarauder.class, BebopSkullCrossbones.class, Forest.class, GrizzlyBears.class})
class RocksteadyMutantMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Bebop")
    void partnerWithSearchesForBebop() {
        Card partner = new BebopSkullCrossbones();
        Forest decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.setHand(player1, List.of(new RocksteadyMutantMarauder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Bebop, Skull & Crossbones");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("Another nontoken creature entering puts a counter on a target creature")
    void nontokenCreatureEntryPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new RocksteadyMutantMarauder());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter trigger can target an opponent's creature")
    void counterTriggerCanTargetOpponentCreature() {
        harness.addToBattlefield(player1, new RocksteadyMutantMarauder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter trigger cannot target a noncreature permanent")
    void counterTriggerCannotTargetLand() {
        harness.addToBattlefield(player1, new RocksteadyMutantMarauder());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void partnerSearchCanBeDeclined() {
        Card partner = new BebopSkullCrossbones();
        Card decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.setHand(player1, List.of(new RocksteadyMutantMarauder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy, partner);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The partner search can target its controller and fail to find Bebop")
    void partnerSearchCanTargetControllerAndFailToFind() {
        Card partner = new BebopSkullCrossbones();
        harness.setLibrary(player1, List.of(partner));
        harness.setHand(player1, List.of(new RocksteadyMutantMarauder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(partner);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting the partner search without Bebop completes without choosing another card")
    void partnerSearchWithoutMatchingCardCompletes() {
        Card decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy));
        harness.setHand(player1, List.of(new RocksteadyMutantMarauder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Rocksteady does not trigger its counter ability for its own entry")
    void ownEntryDoesNotTriggerCounterAbility() {
        harness.setHand(player1, List.of(new RocksteadyMutantMarauder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An allied token creature does not trigger Rocksteady")
    void tokenEntryDoesNotTriggerCounterAbility() {
        Permanent rocksteady = harness.addToBattlefieldAndReturn(player1, new RocksteadyMutantMarauder());
        Card token = new GrizzlyBears();
        token.setToken(true);

        harness.enterBattlefieldAndReturn(player1, token);

        assertThat(rocksteady.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's nontoken creature does not trigger Rocksteady")
    void opponentEntryDoesNotTriggerCounterAbility() {
        Permanent rocksteady = harness.addToBattlefieldAndReturn(player1, new RocksteadyMutantMarauder());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(rocksteady.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The counter trigger can target the entering creature")
    void counterTriggerCanTargetEnteringCreature() {
        harness.addToBattlefield(player1, new RocksteadyMutantMarauder());
        Card enteringCard = new GrizzlyBears();
        harness.castFromHand(player1, enteringCard, "{1}{G}");
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == enteringCard)
                .findFirst().orElseThrow();

        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
