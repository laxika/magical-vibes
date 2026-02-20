package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.SingleTargetSpellTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShuntTest {

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

    @Test
    @DisplayName("Shunt has correct card properties")
    void hasCorrectProperties() {
        Shunt card = new Shunt();

        assertThat(card.getName()).isEqualTo("Shunt");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(SingleTargetSpellTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChangeTargetOfTargetSpellWithSingleTargetEffect.class);
    }

    @Test
    @DisplayName("Casting Shunt requires targeting a spell with a single target")
    void castingRequiresSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Resolving Shunt retargets a single-target spell")
    void resolvingRetargetsSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(bears2PermId);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears1PermId));
    }

    @Test
    @DisplayName("Shunt does nothing if there is no legal new target")
    void doesNothingWithoutAlternativeTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);

        StackEntry boomerangEntry = gd.stack.getLast();
        assertThat(boomerangEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(boomerangEntry.getCard().getName()).isEqualTo("Boomerang");
        assertThat(boomerangEntry.getTargetPermanentId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("Shunt can retarget a player-target spell to another legal player")
    void canRetargetPlayerTargetSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(player1.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Shunt can retarget a spell-targeting spell to a different spell on stack")
    void canRetargetSpellTargetingSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player1, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && !p.getId().equals(bears1PermId))
                .findFirst().orElseThrow().getId();

        Boomerang boomerangA = new Boomerang();
        Boomerang boomerangB = new Boomerang();
        harness.setHand(player1, List.of(boomerangA, boomerangB));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Cancel cancel = new Cancel();
        Shunt shunt = new Shunt();
        harness.setHand(player2, List.of(cancel, shunt));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerangA.getId());
        harness.castInstant(player1, 0, bears2PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cancel.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player2, boomerangB.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Boomerang B resolves first (top of stack), then Cancel fizzles, then Boomerang A resolves.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears1PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Cancel") && log.contains("fizzles"));
    }
}


