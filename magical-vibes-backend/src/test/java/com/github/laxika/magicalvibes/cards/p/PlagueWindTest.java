package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlagueWindTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    private static Card indestructibleCreature() {
        Card card = new Card();
        card.setName("Darksteel Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(null);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Plague Wind has correct card properties")
    void hasCorrectProperties() {
        PlagueWind card = new PlagueWind();

        assertThat(card.getName()).isEqualTo("Plague Wind");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{7}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DestroyAllPermanentsEffect.class);
        DestroyAllPermanentsEffect effect =
                (DestroyAllPermanentsEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.targetTypes()).containsExactly(CardType.CREATURE);
        assertThat(effect.onlyOpponents()).isTrue();
        assertThat(effect.cannotBeRegenerated()).isTrue();
    }

    @Test
    @DisplayName("Casting Plague Wind puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new PlagueWind()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Plague Wind");
    }

    @Test
    @DisplayName("Plague Wind destroys only creatures you do not control")
    void destroysOnlyOpponentsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlagueWind()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Plague Wind does not destroy noncreature permanents you do not control")
    void doesNotDestroyOpponentsNonCreatures() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new PlagueWind()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Howling Mine"));
    }

    @Test
    @DisplayName("Creatures destroyed by Plague Wind cannot be regenerated")
    void ignoresRegenerationShields() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opposingBears = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        opposingBears.setRegenerationShield(2);

        harness.setHand(player1, List.of(new PlagueWind()));
        harness.addMana(player1, ManaColor.BLACK, 9);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Indestructible creatures you do not control survive Plague Wind")
    void indestructibleOpponentCreaturesSurvive() {
        harness.addToBattlefield(player2, indestructibleCreature());
        harness.setHand(player1, List.of(new PlagueWind()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Bear"));
    }
}
