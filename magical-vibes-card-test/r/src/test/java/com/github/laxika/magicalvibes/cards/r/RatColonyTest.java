package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckDefinition;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.DeckValidationService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@CardUsed({RatColony.class, TyphoidRats.class, GrizzlyBears.class, DeepFreeze.class})
class RatColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Rat Colony is 2/1 with no other Rats")
    void baseStatsWithNoOtherRats() {
        Permanent colony = addCreatureReady(player1, new RatColony());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony gets +1/+0 for each other Rat Colony you control")
    void countsOtherRatColonies() {
        Permanent colony = addCreatureReady(player1, new RatColony());
        harness.addToBattlefield(player1, new RatColony());
        harness.addToBattlefield(player1, new RatColony());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony counts other Rats of different names")
    void countsOtherRatsOfDifferentNames() {
        Permanent colony = addCreatureReady(player1, new RatColony());
        harness.addToBattlefield(player1, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony does not count opponent's Rats")
    void doesNotCountOpponentRats() {
        Permanent colony = addCreatureReady(player1, new RatColony());
        harness.addToBattlefield(player2, new RatColony());
        harness.addToBattlefield(player2, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony does not count non-Rat creatures")
    void doesNotCountNonRats() {
        Permanent colony = addCreatureReady(player1, new RatColony());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony bonus updates when other Rats leave the battlefield")
    void bonusUpdatesWhenRatsLeave() {
        Permanent colony = addCreatureReady(player1, new RatColony());
        harness.addToBattlefield(player1, new RatColony());
        harness.addToBattlefield(player1, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Typhoid Rats"));

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Rat Colony updates its bonus when another Rat enters")
    void bonusUpdatesWhenRatEnters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RatColony());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);

        Permanent second = harness.enterBattlefieldAndReturn(player1, new RatColony());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony does not count Rats in other zones")
    void doesNotCountRatsOutsideBattlefield() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new RatColony());
        harness.setHand(player1, List.of(new RatColony()));
        harness.setLibrary(player1, List.of(new RatColony()));
        harness.setGraveyard(player1, List.of(new RatColony()));
        harness.setExile(player1, List.of(new RatColony()));

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing a Rat Colony's abilities removes its bonus but it still counts as a Rat")
    void abilityRemovalStopsOnlyEnchantedColonysBonus() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new RatColony());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RatColony());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        harness.setHand(player2, List.of(new DeepFreeze()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("A deck can contain more than four Rat Colonies")
    void permitsUnlimitedCopiesInDeck() {
        var validator = new DeckValidationService(mock(DeckLegalityRegistry.class));
        var colonies = IntStream.range(0, 7)
                .<Card>mapToObj(i -> new RatColony())
                .toList();

        assertThat(validator.validate(new DeckDefinition(colonies, List.of(), null), DeckFormat.CASUAL)
                .valid()).isTrue();
    }
}
