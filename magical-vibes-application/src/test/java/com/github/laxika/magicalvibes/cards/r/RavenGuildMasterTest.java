package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenGuildMaster.class, ScornfulEgotist.class, GildedLight.class})
class RavenGuildMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top ten cards of the damaged player's library")
    void combatDamageExilesTopTenCards() {
        addAttackingRaven();
        Card remainingCard = new ScornfulEgotist();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new ScornfulEgotist());
        }
        library.add(remainingCard);
        library.add(new ScornfulEgotist());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).contains(remainingCard);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyElementsOf(library.subList(0, 10));
        assertThat(gd.exiledCards.stream()
                .filter(entry -> player2.getId().equals(entry.ownerId()))
                .toList())
                .hasSize(10)
                .allMatch(entry -> entry.sourcePermanentId() == null);
    }

    @Test
    @DisplayName("Combat damage exiles the entire library when fewer than ten cards remain")
    void combatDamageExilesEntireSmallLibrary() {
        addAttackingRaven();
        harness.setLibrary(player2, List.of(new ScornfulEgotist(), new ScornfulEgotist()));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A blocked Raven Guild Master does not exile cards")
    void noTriggerWhenBlocked() {
        addAttackingRaven();
        Permanent blocker = addCreatureReady(player2, new ScornfulEgotist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        List<Card> library = List.of(new ScornfulEgotist(), new ScornfulEgotist());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(1));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Morph can turn Raven Guild Master face up for {2}{U}{U}")
    void morphsFaceDownAndCanBeTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new RavenGuildMaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raven = findPermanent(player1, "Raven Guild Master");
        assertThat(raven.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raven));
        harness.passBothPriorities();

        assertThat(raven.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("A face-down Raven Guild Master does not have its combat-damage ability")
    void faceDownRavenDoesNotTrigger() {
        harness.setHand(player1, List.of(new RavenGuildMaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raven = findPermanent(player1, "Raven Guild Master");
        raven.setAttacking(true);
        harness.setLibrary(player2, List.of(new ScornfulEgotist(), new ScornfulEgotist()));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The combat-damage ability still resolves when the damaged player has shroud")
    void combatDamageTriggerIsNotStoppedByPlayerShroud() {
        harness.setHand(player2, List.of(new GildedLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        addAttackingRaven();
        List<Card> library = List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyElementsOf(library);
    }

    private Permanent addAttackingRaven() {
        Permanent raven = addCreatureReady(player1, new RavenGuildMaster());
        raven.setAttacking(true);
        return raven;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
