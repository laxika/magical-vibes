package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JhoiraWeatherlightCaptain.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        Spellbook.class, MoxAmber.class, TheFlameOfKeld.class})
class JhoiraWeatherlightCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers draw a card")
    void artifactSpellTriggersDrawCard() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Jhoira, Weatherlight Captain"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability draws a card")
    void artifactTriggerDrawsCard() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new Spellbook()));

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top, Spellbook below)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hand was 1 card, cast Spellbook (0 cards), then drew 1 card = 1 card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers draw a card")
    void legendarySpellTriggersDrawCard() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Jhoira, Weatherlight Captain"));
    }

    @Test
    @DisplayName("Casting a non-historic creature does not trigger draw")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Jhoira")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Casting two artifact spells draws a card each time")
    void multipleHistoricSpellsTriggerMultipleTimes() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        // Cast first artifact
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (draw)

        GameData gd = harness.getGameData();
        // Started with 2 cards, cast 2 (0 cards), drew 2 (2 cards)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Casting a Saga draws before the Saga resolves")
    void sagaSpellDrawsBeforeResolving() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new TheFlameOfKeld()));
        AdelizTheCinderWind drawnCard = new AdelizTheCinderWind();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TheFlameOfKeld);
    }

    @Test
    @DisplayName("A legendary artifact draws only one card")
    void legendaryArtifactTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new MoxAmber()));
        AdelizTheCinderWind drawnCard = new AdelizTheCinderWind();
        harness.setLibrary(player1, List.of(drawnCard, new TheFlameOfKeld()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Jhoira does not trigger from her own casting or entering")
    void castingJhoiraDoesNotDraw() {
        harness.setHand(player1, List.of(new JhoiraWeatherlightCaptain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof JhoiraWeatherlightCaptain);
    }
}
