package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CloneTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Clone has correct card properties")
    void hasCorrectProperties() {
        Clone card = new Clone();

        assertThat(card.getName()).isEqualTo("Clone");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(0);
        assertThat(card.getToughness()).isEqualTo(0);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyCreatureOnEnterEffect.class);
    }

    // ===== Copying a creature =====

    @Test
    @DisplayName("Clone copies a creature's power and toughness")
    void copiesPowerAndToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        // Should be prompted for may ability
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompted to choose a creature
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Clone should now be on the battlefield with Grizzly Bears' stats
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent clonePerm = bf.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                        && p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getPower()).isEqualTo(2);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Clone copies a creature's keywords (e.g., flying)")
    void copiesKeywords() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(clonePerm.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Clone copies a creature's subtypes")
    void copiesSubtypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    // ===== Leaving the battlefield =====

    @Test
    @DisplayName("Clone goes to graveyard as Clone (not the copied name) when destroyed")
    void goesToGraveyardAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Now destroy the Clone (which looks like Grizzly Bears on the battlefield)
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();

        // Remove it manually (simulating destruction)
        gd.playerBattlefields.get(player1.getId()).remove(clonePerm);
        gd.playerGraveyards.get(player1.getId()).add(clonePerm.getOriginalCard());

        // In graveyard it should be "Clone", not "Grizzly Bears"
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Clone"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Clone returns to hand as Clone when bounced")
    void returnsToHandAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Simulate bouncing by using the Permanent's getOriginalCard
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();

        gd.playerBattlefields.get(player1.getId()).remove(clonePerm);
        gd.playerHands.get(player1.getId()).add(clonePerm.getOriginalCard());

        // In hand it should be "Clone", not "Grizzly Bears"
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Clone"));
    }

    // ===== Legend rule =====

    @Test
    @DisplayName("Clone triggers legend rule when copying a legendary creature")
    void triggersLegendRule() {
        ChoMannoRevolutionary choManno = new ChoMannoRevolutionary();
        harness.addToBattlefield(player1, choManno);
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Accept to copy
        harness.handleMayAbilityChosen(player1, true);

        // Choose to copy Cho-Manno
        UUID choMannoId = harness.getPermanentId(player1, "Cho-Manno, Revolutionary");
        harness.handlePermanentChosen(player1, choMannoId);

        GameData gd = harness.getGameData();

        // Legend rule should be triggered — player should be asked to choose which to keep
        assertThat(gd.permanentChoiceContext).isInstanceOf(PermanentChoiceContext.LegendRule.class);
        assertThat(((PermanentChoiceContext.LegendRule) gd.permanentChoiceContext).cardName()).isEqualTo("Cho-Manno, Revolutionary");
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());
    }

    // ===== Declining / no creatures =====

    @Test
    @DisplayName("Clone enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Decline to copy
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Clone should be dead (0/0 killed by SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Clone"));

        // Clone should be in graveyard as "Clone"
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Clone"));
    }

    @Test
    @DisplayName("Clone enters as 0/0 and dies when no creatures on battlefield")
    void diesWhenNoCreatures() {
        // No creatures on any battlefield
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Clone should be dead (0/0 killed by SBA — no creatures to copy)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Clone"));

        // Clone should be in graveyard as "Clone"
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Clone"));
    }
}
