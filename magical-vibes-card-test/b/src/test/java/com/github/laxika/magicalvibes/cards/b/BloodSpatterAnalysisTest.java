package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeadlyCoverUp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VengefulCreeper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodSpatterAnalysis.class, Forest.class, GrizzlyBears.class, Shock.class,
        DeadlyCoverUp.class, NoviceInspector.class, VengefulCreeper.class})
class BloodSpatterAnalysisTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToTargetCreatureAnOpponentControls() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BloodSpatterAnalysis analysis = new BloodSpatterAnalysis();
        harness.setHand(player1, List.of(analysis));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    void deathTriggerMillsCountersSacrificesAndChoosesCreatureAfterSacrifice() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        analysis.setCounterCount(CounterType.BLOODSTAIN, 4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest milledCard = new Forest();
        GrizzlyBears returnTarget = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setGraveyard(player1, List.of(returnTarget));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnTarget.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(analysis.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(returnTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnTarget.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returnTarget.getId()));
    }

    @Test
    void friendlyCreatureDeathMillsAndAddsOneCounterWithoutSacrificingBelowFive() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        analysis.setCounterCount(CounterType.BLOODSTAIN, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        Forest milledCard = new Forest();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(milledCard, remainingCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood Spatter Analysis");
        assertThat(analysis.getCounterCount(CounterType.BLOODSTAIN)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void simultaneousCreatureDeathsTriggerOnlyOnce() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.addToBattlefield(player2, new NoviceInspector());
        Forest milledCard = new Forest();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(milledCard, remainingCard));
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(analysis.getCounterCount(CounterType.BLOODSTAIN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        harness.assertNotOnBattlefield(player1, "Novice Inspector");
        harness.assertNotOnBattlefield(player2, "Novice Inspector");
    }

    @Test
    void fifthCounterCanReturnCreatureMilledByTheSameTrigger() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        analysis.setCounterCount(CounterType.BLOODSTAIN, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        NoviceInspector milledCreature = new NoviceInspector();
        harness.setLibrary(player1, List.of(milledCreature));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(milledCreature.getId());
        harness.assertNotOnBattlefield(player1, "Blood Spatter Analysis");
        harness.handleMultipleCardsChosen(player1, List.of(milledCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(milledCreature);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(milledCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(milledCreature);
    }

    @Test
    void emptyLibraryAndNoCreatureInGraveyardDoNotPreventFifthCounterSacrifice() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        analysis.setCounterCount(CounterType.BLOODSTAIN, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blood Spatter Analysis");
        harness.assertInGraveyard(player1, "Blood Spatter Analysis");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enterTriggerDealsExactlyThreeDamageAndDoesNotDamageYourCreature() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new VengefulCreeper());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new VengefulCreeper());
        harness.setHand(player1, List.of(new BloodSpatterAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opposingCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(friendlyCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Vengeful Creeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureKilledByEnterTriggerCausesMillAndBloodstainCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        Forest milledCard = new Forest();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setHand(player1, List.of(new BloodSpatterAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Novice Inspector");
        harness.assertOnBattlefield(player1, "Blood Spatter Analysis");
        Permanent analysis = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BloodSpatterAnalysis)
                .findFirst().orElseThrow();
        assertThat(analysis.getCounterCount(CounterType.BLOODSTAIN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
    }
}
