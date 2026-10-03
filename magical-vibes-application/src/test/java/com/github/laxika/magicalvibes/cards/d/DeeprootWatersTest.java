package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.k.KumenasSpeaker;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootWaters.class, KumenasSpeaker.class, RaptorHatchling.class, Demystify.class})
class DeeprootWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Merfolk spell triggers token creation")
    void merfolkCastTriggersTokenCreation() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Deeproot Waters"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // A Merfolk token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Merfolk")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.BLUE
                        && p.getCard().getSubtypes().contains(CardSubtype.MERFOLK)
                        && p.getCard().getKeywords().contains(Keyword.HEXPROOF)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Non-Merfolk spell does not trigger Deeproot Waters")
    void nonMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new RaptorHatchling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Stack should only have the creature spell, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting Merfolk does not trigger Deeproot Waters")
    void opponentMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootWaters());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new KumenasSpeaker()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        // No triggered ability from Deeproot Waters
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Deeproot Waters"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Casting two Merfolk spells creates two tokens")
    void multipleMerfolkCastsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new KumenasSpeaker(), new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Cast first Merfolk
        harness.castCreature(player1, 0);
        // Resolve triggered ability
        harness.passBothPriorities();
        // Resolve the creature spell
        harness.passBothPriorities();

        // Cast second Merfolk
        harness.castCreature(player1, 0);
        // Resolve triggered ability
        harness.passBothPriorities();
        // Resolve the creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Merfolk") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Deeproot Waters triggers independently for one Merfolk spell")
    void multipleWatersCreateOneTokenEach() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("A Merfolk entering without being cast does not trigger Deeproot Waters")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootWaters());

        harness.enterBattlefieldAndReturn(player1, new KumenasSpeaker());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("The cast trigger still creates a token after Deeproot Waters is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        var waters = harness.addToBattlefieldAndReturn(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, waters.getId());

        harness.assertInGraveyard(player1, "Deeproot Waters");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
