package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
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
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreasureHunter;
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
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyPermanentOnEnterEffect.class);
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
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompted to choose a creature
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
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
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.LegendRule.class);
        assertThat(((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName()).isEqualTo("Cho-Manno, Revolutionary");
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
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

    // ===== Copied creature's ETB effects =====

    @Test
    @DisplayName("Clone copying a creature with mandatory ETB triggers that effect")
    void copiedCreatureMandatoryETBFires() {
        // Angel of Mercy has ETB: gain 3 life
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        harness.handlePermanentChosen(player1, angelId);

        GameData gd = harness.getGameData();

        // Clone should be on the battlefield as Angel of Mercy
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Angel of Mercy");

        // The copied Angel of Mercy's ETB "gain 3 life" should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angel of Mercy"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player 1 should have gained 3 life (20 → 23)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Angelic Chorus sees cloned creature's toughness, not 0/0")
    void angelicChorusSeesCopiedToughness() {
        // Angelic Chorus: whenever a creature enters under your control, gain life equal to its toughness
        harness.addToBattlefield(player1, new AngelicChorus());
        // Grizzly Bears is a 2/2
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

        // Angelic Chorus should have triggered with toughness=2 (not 0)
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angelic Chorus"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player 1 should have gained 2 life (20 → 22), proving Angelic Chorus saw toughness=2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Clone copying a creature with may-based ETB presents the may prompt")
    void copiedCreatureMayETBPromptAppears() {
        // Treasure Hunter has may ETB: "You may return an artifact from your graveyard to your hand"
        harness.addToBattlefield(player2, new TreasureHunter());
        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // First may prompt: Clone's own "you may copy" prompt
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Choose to copy Treasure Hunter
        UUID hunterId = harness.getPermanentId(player2, "Treasure Hunter");
        harness.handlePermanentChosen(player1, hunterId);

        // Clone should be on the battlefield as Treasure Hunter
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Treasure Hunter");

        // Second may prompt: copied Treasure Hunter's may ETB should now be presented
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Decline it (no artifacts in graveyard anyway)
        harness.handleMayAbilityChosen(player1, false);

        // Game should proceed normally — no awaiting input
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }
}


