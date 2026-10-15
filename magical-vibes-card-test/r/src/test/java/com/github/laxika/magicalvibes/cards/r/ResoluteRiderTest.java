package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoluteRider.class, BakeIntoAPie.class})
class ResoluteRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink ability is payable with black mana")
    void gainsLifelink() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Indestructible ability is payable with white mana")
    void gainsIndestructible() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Lifelink and indestructible wear off at end of turn")
    void temporaryAbilitiesWearOffAtEndOfTurn() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Both abilities accept mixed hybrid mana while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new ResoluteRider());
        rider.setSummoningSick(true);
        rider.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(rider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated lifelink activation gains life only once for combat damage")
    void repeatedLifelinkDoesNotMultiplyLifeGain() {
        addCreatureReady(player1, new ResoluteRider());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Indestructible activated in response prevents destruction")
    void indestructibleStopsDestroySpell() {
        addCreatureReady(player1, new ResoluteRider());
        harness.setHand(player2, List.of(new BakeIntoAPie()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Resolute Rider"));

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Resolute Rider");
        harness.assertNotInGraveyard(player1, "Resolute Rider");
        harness.assertOnBattlefield(player2, "Food");
    }
}
