package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwilightShepherd.class, ObsidianBattleAxe.class, Naturalize.class, GrizzlyBears.class,
        DoomBlade.class})
class TwilightShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a noncreature permanent that was put into your graveyard from the battlefield this turn")
    void etbReturnsNoncreatureCardFromBattlefieldThisTurn() {
        Card axe = new ObsidianBattleAxe();
        harness.addToBattlefield(player1, axe);

        // Destroy the artifact so it hits the graveyard from the battlefield this turn.
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(axe.getId()));

        // Cast Twilight Shepherd; its ETB should return the axe to hand.
        harness.castFromHand(player1, new TwilightShepherd(), "{3}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(axe.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(axe.getId()));
    }

    @Test
    @DisplayName("ETB does not return cards that were not put into your graveyard from the battlefield this turn")
    void etbDoesNotReturnCardsNotFromBattlefield() {
        Card alreadyInGraveyard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));

        harness.castFromHand(player1, new TwilightShepherd(), "{3}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
    }

    @Test
    @DisplayName("ETB returns all qualifying cards from its controller's graveyard only")
    void etbReturnsAllQualifyingCardsFromControllerGraveyardOnly() {
        Card firstAxe = new ObsidianBattleAxe();
        Card secondAxe = new ObsidianBattleAxe();
        Card opposingAxe = new ObsidianBattleAxe();
        Permanent firstAxePermanent = harness.addToBattlefieldAndReturn(player1, firstAxe);
        Permanent secondAxePermanent = harness.addToBattlefieldAndReturn(player1, secondAxe);
        Permanent opposingAxePermanent = harness.addToBattlefieldAndReturn(player2, opposingAxe);

        harness.setHand(player1, List.of(new Naturalize(), new Naturalize(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveInstant(player1, 0, firstAxePermanent.getId());
        harness.castAndResolveInstant(player1, 0, secondAxePermanent.getId());
        harness.castAndResolveInstant(player1, 0, opposingAxePermanent.getId());

        harness.castFromHand(player1, new TwilightShepherd(), "{3}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstAxe.getId(), secondAxe.getId())
                .doesNotContain(opposingAxe.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opposingAxe.getId()));
    }

    @Test
    @DisplayName("Persist returns Twilight Shepherd with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new TwilightShepherd());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Twilight Shepherd"));
        resolveAllTriggers();

        Permanent shepherd = findPermanent(player1, "Twilight Shepherd");
        assertThat(shepherd.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(shepherd.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Persist does not return Twilight Shepherd when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new TwilightShepherd());
        shepherd.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, shepherd.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Twilight Shepherd");
        harness.assertInGraveyard(player1, "Twilight Shepherd");
    }

    @Test
    @DisplayName("Persist triggers the entry ability again and returns other creatures that died this turn")
    void persistEntryReturnsOtherDeadCreatures() {
        Card firstShepherd = new TwilightShepherd();
        Permanent first = harness.addToBattlefieldAndReturn(player1, firstShepherd);
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Card secondShepherd = new TwilightShepherd();
        Permanent second = harness.addToBattlefieldAndReturn(player1, secondShepherd);
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.castInstant(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(firstShepherd.getId()).doesNotContain(secondShepherd.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(firstShepherd.getId(), secondShepherd.getId());
        assertThat(findPermanent(player1, "Twilight Shepherd").getCard().getId())
                .isEqualTo(secondShepherd.getId());
        assertThat(findPermanent(player1, "Twilight Shepherd").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(1);
        harness.assertInGraveyard(player1, "Doom Blade");
    }

    @Test
    @DisplayName("Entry ability includes its source if it dies before the ability resolves")
    void entryReturnsSourceThatDiesInResponse() {
        Card card = new TwilightShepherd();
        Permanent shepherd = harness.enterBattlefieldAndReturn(player1, card);
        shepherd.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, shepherd.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Twilight Shepherd");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(card.getId());
        harness.assertInGraveyard(player1, "Doom Blade");
    }

    @Test
    @DisplayName("Entry ability does not return a permanent that died on a previous turn")
    void entryDoesNotReturnPreviousTurnDeaths() {
        Card axe = new ObsidianBattleAxe();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, axe);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TwilightShepherd(), "{3}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(axe.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(axe.getId());
    }

    @Test
    @DisplayName("Entry with an empty graveyard resolves without any choice")
    void entryWithEmptyGraveyardResolves() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new TwilightShepherd(), "{3}{W}{W}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Twilight Shepherd");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Twilight Shepherd");
    }

    @Test
    @DisplayName("Flying prevents ground blockers and vigilance leaves the attacking Shepherd untapped")
    void flyingAndVigilanceApplyInCombat() {
        Permanent shepherd = addCreatureReady(player1, new TwilightShepherd());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(shepherd.isAttacking()).isTrue();
        assertThat(shepherd.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
