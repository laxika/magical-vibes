package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BurnDownTheHouse.class, ChandraNalaar.class, DoomBlade.class, GrizzlyBears.class})
class BurnDownTheHouseTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 5 damage to creatures and planeswalkers")
    void damageMode() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 8);

        castBurnDownTheHouse(0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Token mode creates three hasty Devils")
    void tokenModeCreatesHastyDevils() {
        castBurnDownTheHouse(1);

        List<Permanent> devils = findPermanents(player1, "Devil");
        assertThat(devils).hasSize(3);
        assertThat(devils).allSatisfy(devil -> {
            assertThat(devil.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(devil.getCard().getSubtypes()).containsExactly(CardSubtype.DEVIL);
            assertThat(devil.hasKeyword(Keyword.HASTE)).isTrue();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devils).allSatisfy(devil ->
                assertThat(devil.hasKeyword(Keyword.HASTE)).isFalse());
    }

    @Test
    @DisplayName("A Devil that dies deals 1 damage to a target player")
    void devilDeathDealsDamage() {
        castBurnDownTheHouse(1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, devil.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage mode leaves players unharmed and does not create Devils")
    void damageModeDoesNotDamagePlayersOrCreateTokens() {
        castBurnDownTheHouse(0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    @DisplayName("Token mode leaves existing creatures and planeswalkers unharmed")
    void tokenModeDoesNotDealMassDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());

        castBurnDownTheHouse(1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(findPermanents(player1, "Devil")).hasSize(3);
    }

    @Test
    @DisplayName("New Devils can attack immediately and each deals one combat damage")
    void devilsCanAttackOnTheTurnTheyAreCreated() {
        castBurnDownTheHouse(1);

        declareAttackers(List.of(0, 1, 2));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A Devil's death trigger can damage a creature")
    void devilDeathCanTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBurnDownTheHouse(1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, devil.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Devil")).hasSize(2);
    }

    @Test
    @DisplayName("A Devil's death trigger can remove loyalty from a planeswalker")
    void devilDeathCanTargetPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        castBurnDownTheHouse(1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, devil.getId());
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(findPermanents(player1, "Devil")).hasSize(2);
    }

    private void castBurnDownTheHouse(int mode) {
        harness.setHand(player1, List.of(new BurnDownTheHouse()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, mode);
    }
}
