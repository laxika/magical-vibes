package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraspingCurrentTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Has correct effects")
    void hasCorrectEffects() {
        GraspingCurrent card = new GraspingCurrent();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ReturnTargetPermanentToHandEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(SearchLibraryAndOrGraveyardForNamedCardToHandEffect.class);
    }

    @Test
    @DisplayName("Search effect targets Jace, Ingenious Mind-Mage by name")
    void searchEffectTargetsCorrectName() {
        GraspingCurrent card = new GraspingCurrent();

        SearchLibraryAndOrGraveyardForNamedCardToHandEffect searchEffect =
                (SearchLibraryAndOrGraveyardForNamedCardToHandEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(searchEffect.cardName()).isEqualTo("Jace, Ingenious Mind-Mage");
    }

    // ===== Bounce two creatures =====

    @Test
    @DisplayName("Bounces two target creatures to their owners' hands")
    void bouncesTwoCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(2);
    }

    // ===== Bounce one creature =====

    @Test
    @DisplayName("Can target only one creature")
    void bouncesOneCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Cannot target non-creatures =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Search finds Jace in graveyard =====

    @Test
    @DisplayName("Bounces creatures and finds Jace in graveyard")
    void bouncesAndFindsJaceInGraveyard() {
        Card jace = new Card();
        jace.setName("Jace, Ingenious Mind-Mage");
        jace.setType(CardType.PLANESWALKER);
        jace.setManaCost("{4}{U}{U}");
        jace.setColor(CardColor.BLUE);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Jace moved from graveyard to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jace, Ingenious Mind-Mage"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Jace, Ingenious Mind-Mage"));
    }

    // ===== Search when Jace not present =====

    @Test
    @DisplayName("Does not find Jace when not in graveyard or library")
    void doesNotFindJace() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature still bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // No Jace found
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
