package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonasteryRaid.class, Forest.class, Reverberate.class})
class MonasteryRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards when cast normally")
    void normalCastExilesTopTwoCards() {
        Card first = new Forest();
        Card second = new MonasteryRaid();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Exiles X cards when cast for freerunning")
    void freerunningCastExilesXCards() {
        Card first = new Forest();
        Card second = new MonasteryRaid();
        Card third = new Forest();
        Card remaining = new MonasteryRaid();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Normal casting still exiles two cards when freerunning is available")
    void normalCastDoesNotUseFreerunningBranch() {
        Card first = new Forest();
        Card second = new MonasteryRaid();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second);
    }

    @Test
    @DisplayName("Freerunning with zero X exiles nothing instead of two cards")
    void freerunningWithZeroXExilesNothing() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Monastery Raid");
    }

    @Test
    @DisplayName("Freerunning is unavailable without qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 3, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Monastery Raid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Freerunning exiles only the available cards when X exceeds library size")
    void freerunningWithShortLibrary() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.castFromExile(player1, onlyCard.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Monastery Raid");
    }

    @Test
    @DisplayName("Exiled spells require payment of their normal mana cost")
    void exiledSpellsRequireMana() {
        Card spell = new MonasteryRaid();
        harness.setLibrary(player1, List.of(spell, new Forest()));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof MonasteryRaid).hasSize(2);
    }

    @Test
    @DisplayName("Playing exiled lands does not grant additional land plays")
    void exiledLandsRespectLandPlayLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Play permission lasts through your next turn and then expires")
    void playPermissionExpiresAfterNextTurn() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("A copy retains the original spell's freerunning payment and X")
    void copyRetainsFreerunningPayment() {
        Card raid = new MonasteryRaid();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, remaining));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(raid));
        harness.setHand(player2, List.of(new Reverberate()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, raid.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }
}
