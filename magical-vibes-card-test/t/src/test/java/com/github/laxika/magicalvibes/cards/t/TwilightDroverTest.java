package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.s.ScatterTheSeeds;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwilightDrover.class, BorosSwiftblade.class, Putrefy.class, ScatterTheSeeds.class,
        PeelFromReality.class})
class TwilightDroverTest extends BaseCardTest {

    @Test
    @DisplayName("A creature token leaving the battlefield puts a +1/+1 counter on Twilight Drover")
    void tokenLeavingBattlefieldAddsCounter() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new TwilightDrover());
        Permanent token = addSaprolingToken(player2);

        destroyPermanent(player1, token);

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player2, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("A nontoken creature leaving the battlefield does not trigger Twilight Drover")
    void nontokenCreatureLeavingBattlefieldDoesNotAddCounter() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new TwilightDrover());
        Permanent creature = addCreatureReady(player2, new BorosSwiftblade());

        destroyPermanent(player1, creature);

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Boros Swiftblade")).isEmpty();
    }

    @Test
    @DisplayName("A creature token returning to hand triggers Twilight Drover")
    void tokenReturningToHandAddsCounter() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new TwilightDrover());
        Permanent token = addSaprolingToken(player1);
        Permanent opposingCreature = addCreatureReady(player2, new BorosSwiftblade());

        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, List.of(token.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Boros Swiftblade")).isEmpty();
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates two white Spirit tokens with flying")
    void removesCounterAndCreatesTwoSpirits() {
        Permanent drover = addCreatureReady(player1, new TwilightDrover());
        drover.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("The token-making ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        addCreatureReady(player1, new TwilightDrover());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The token-making ability cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        Permanent drover = addCreatureReady(player1, new TwilightDrover());
        drover.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning tokens controlled by both players produces one counter per token")
    void tokensFromBothPlayersLeavingAddSeparateCounters() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new TwilightDrover());
        Permanent ownToken = addSaprolingToken(player1);
        Permanent opposingToken = addSaprolingToken(player2);

        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, List.of(ownToken.getId(), opposingToken.getId()));
        resolveAllTriggers();

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Drover pays its counter immediately and creates 1/1 tokens")
    void abilityDoesNotRequireTappingOrHasteAndPaysCounterBeforeResolution() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new TwilightDrover());
        drover.setSummoningSick(true);
        drover.tap();
        drover.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        harness.passBothPriorities();

        assertThat(drover.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(drover.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(spirit -> {
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The activated ability still creates tokens after Twilight Drover is destroyed")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent drover = addCreatureReady(player1, new TwilightDrover());
        drover.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, drover.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Twilight Drover");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    private Permanent addSaprolingToken(Player player) {
        harness.setHand(player, List.of(new ScatterTheSeeds()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player);
        harness.castAndResolveInstant(player, 0);
        return findPermanents(player, "Saproling").getFirst();
    }

    private void destroyPermanent(Player player, Permanent target) {
        harness.setHand(player, List.of(new Putrefy()));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player);
        harness.castAndResolveInstant(player, 0, target.getId());
        resolveAllTriggers();
    }
}
