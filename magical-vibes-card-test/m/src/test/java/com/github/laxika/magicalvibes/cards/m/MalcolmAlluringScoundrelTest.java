package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BitterTriumph;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldfuryStrider;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({MalcolmAlluringScoundrel.class, Forest.class, GoldfuryStrider.class,
        BitterTriumph.class, LeylineOfTheVoid.class})
class MalcolmAlluringScoundrelTest extends BaseCardTest {

    @Test
    void fourthChorusCounterOffersTheDiscardedCardAndCastsItForFree() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(malcolm.getCounterCount(CounterType.CHORUS)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldfury Strider");
        harness.assertOnBattlefield(player1, "Malcolm, Alluring Scoundrel");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void aDiscardedLandIsNotOfferedForCasting() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 4);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void fourCountersAreRememberedIfMalcolmLeavesBeforeResolution() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 4);
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(malcolm);
        gd.playerGraveyards.get(player1.getId()).add(malcolm.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldfury Strider");
    }

    @Test
    void castingChoiceOccursDuringTheOriginalTriggerResolution() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void belowFourCountersStillDrawsAndDiscardsWithoutOfferingACast() {
        Permanent malcolm = addAttacker();
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(malcolm.getCounterCount(CounterType.CHORUS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Goldfury Strider");
    }

    @Test
    void controllerCanDeclineTheFreeCast() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goldfury Strider");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({MalcolmAlluringScoundrel.class, GoldfuryStrider.class, Forest.class, LeylineOfTheVoid.class})
    void discardedCardCanBeCastWhenAReplacementExilesIt() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, List.of(new GoldfuryStrider()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldfury Strider");
    }

    @Test
    @CardUsed({MalcolmAlluringScoundrel.class, BitterTriumph.class})
    void freeCastingDoesNotWaiveUnpayableAdditionalCosts() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BitterTriumph()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, malcolm.getId());
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof BitterTriumph);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bitter Triumph");
        harness.assertLife(player1, 2);
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MalcolmAlluringScoundrel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
