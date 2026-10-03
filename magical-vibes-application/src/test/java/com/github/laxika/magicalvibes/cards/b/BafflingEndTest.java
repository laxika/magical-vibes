package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.cards.s.StampedingHorncrest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.f.FrilledDeathspitter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BafflingEnd.class, SunSentinel.class, StampedingHorncrest.class, Naturalize.class, FrilledDeathspitter.class})
class BafflingEndTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent creature with mana value 3 or less")
    void etbExilesSmallOpponentCreature() {
        harness.addToBattlefield(player2, new SunSentinel());
        UUID bearsId = harness.getPermanentId(player2, "Sun Sentinel");

        castBafflingEnd(bearsId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sun Sentinel"));
    }

    @Test
    @DisplayName("Cannot target an opponent creature with mana value greater than 3")
    void cannotTargetLargeOpponentCreature() {
        harness.addToBattlefield(player2, new StampedingHorncrest());
        UUID giantId = harness.getPermanentId(player2, "Stampeding Horncrest");
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, giantId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Baffling End leaves, a target opponent creates a 3/3 Dinosaur with trample")
    void leavingBattlefieldGivesTargetOpponentDinosaur() {
        harness.addToBattlefield(player2, new SunSentinel());
        UUID bearsId = harness.getPermanentId(player2, "Sun Sentinel");
        castBafflingEnd(bearsId);
        resolveAllTriggers();

        UUID bafflingEndId = harness.getPermanentId(player1, "Baffling End");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bafflingEndId);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sun Sentinel"));
        Permanent dinosaur = findPermanent(player2, "Dinosaur");
        assertThat(dinosaur.getEffectivePower()).isEqualTo(3);
        assertThat(dinosaur.getEffectiveToughness()).isEqualTo(3);
        assertThat(dinosaur.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(dinosaur.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("A creature with mana value exactly three is a legal target")
    void exilesCreatureAtManaValueLimit() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        castBafflingEnd(harness.getPermanentId(player2, "Frilled Deathspitter"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Frilled Deathspitter");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Frilled Deathspitter"));
    }

    @Test
    @DisplayName("Cannot target a creature controlled by Baffling End's controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new SunSentinel());
        UUID sentinelId = harness.getPermanentId(player1, "Sun Sentinel");
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, sentinelId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The entry trigger still exiles its target after Baffling End leaves")
    void entryTriggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new SunSentinel());
        castBafflingEnd(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();

        UUID bafflingEndId = harness.getPermanentId(player1, "Baffling End");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bafflingEndId);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Dinosaur");
        harness.assertNotOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sun Sentinel"));
    }

    @Test
    @DisplayName("Leaving creates a Dinosaur even when there was no creature to exile")
    void leaveTriggerDoesNotRequireExiledCreature() {
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        UUID bafflingEndId = harness.getPermanentId(player1, "Baffling End");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bafflingEndId);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Dinosaur");
        harness.assertNotOnBattlefield(player1, "Dinosaur");
    }

    private void castBafflingEnd(UUID targetId) {
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, targetId);
    }
}
