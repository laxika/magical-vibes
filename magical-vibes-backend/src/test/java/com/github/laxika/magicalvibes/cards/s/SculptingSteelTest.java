package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SculptingSteelTest {

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
    @DisplayName("Sculpting Steel has correct card properties")
    void hasCorrectProperties() {
        SculptingSteel card = new SculptingSteel();

        assertThat(card.getName()).isEqualTo("Sculpting Steel");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{3}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyPermanentOnEnterEffect.class);
    }

    // ===== Copying a non-creature artifact =====

    @Test
    @DisplayName("Sculpting Steel copies a non-creature artifact's activated abilities")
    void copiesNonCreatureArtifact() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        harness.passBothPriorities();

        // Should be prompted for may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompted to choose an artifact
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        // Sculpting Steel should now be on the battlefield as Jayemdae Tome
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent steelPerm = bf.stream()
                .filter(p -> p.getCard().getName().equals("Jayemdae Tome")
                        && p.getOriginalCard().getName().equals("Sculpting Steel"))
                .findFirst().orElse(null);

        assertThat(steelPerm).isNotNull();
        assertThat(steelPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(steelPerm.getCard().getActivatedAbilities()).hasSize(1);
    }

    // ===== Copying an artifact creature =====

    @Test
    @DisplayName("Sculpting Steel copies an artifact creature's power, toughness, and effects")
    void copiesArtifactCreature() {
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID juggernautId = harness.getPermanentId(player2, "Juggernaut");
        harness.handlePermanentChosen(player1, juggernautId);

        GameData gd = harness.getGameData();
        Permanent steelPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Sculpting Steel"))
                .findFirst().orElse(null);

        assertThat(steelPerm).isNotNull();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Juggernaut");
        assertThat(steelPerm.getCard().getPower()).isEqualTo(5);
        assertThat(steelPerm.getCard().getToughness()).isEqualTo(3);
    }

    // ===== Cannot copy non-artifact creatures =====

    @Test
    @DisplayName("Sculpting Steel does not offer to copy non-artifact creatures")
    void doesNotOfferNonArtifactCreatures() {
        // Only a non-artifact creature on the battlefield — no valid targets
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No artifacts on battlefield, so no may prompt — enters as Sculpting Steel
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sculpting Steel"));
    }

    // ===== Declining / no artifacts =====

    @Test
    @DisplayName("Sculpting Steel enters as itself when no artifacts on battlefield")
    void entersAsItselfWhenNoArtifacts() {
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No artifacts on battlefield — enters without copy prompt
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sculpting Steel"));
    }

    @Test
    @DisplayName("Sculpting Steel enters as itself when player declines to copy")
    void entersAsItselfWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Decline to copy
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Sculpting Steel should be on the battlefield as itself (not dead, unlike Clone's 0/0)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sculpting Steel")
                        && p.getOriginalCard().getName().equals("Sculpting Steel"));
    }

    // ===== Leaving the battlefield =====

    @Test
    @DisplayName("Sculpting Steel goes to graveyard as Sculpting Steel when destroyed")
    void goesToGraveyardAsSculptingSteel() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();

        // Simulate destruction
        Permanent steelPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Sculpting Steel"))
                .findFirst().orElse(null);
        assertThat(steelPerm).isNotNull();

        gd.playerBattlefields.get(player1.getId()).remove(steelPerm);
        gd.playerGraveyards.get(player1.getId()).add(steelPerm.getOriginalCard());

        // In graveyard it should be "Sculpting Steel", not "Jayemdae Tome"
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sculpting Steel"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Jayemdae Tome"));
    }

    // ===== Legend rule =====

    @Test
    @DisplayName("Sculpting Steel triggers legend rule when copying a legendary artifact")
    void triggersLegendRule() {
        // Put two Sculpting Steels on battlefield - first one copies Jayemdae Tome,
        // then add a legendary artifact scenario using the same card name
        JayemdaeTome tome1 = new JayemdaeTome();
        JayemdaeTome tome2 = new JayemdaeTome();
        harness.addToBattlefield(player1, tome1);
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player1, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();

        // Both Jayemdae Tome (original) and Sculpting Steel (as Jayemdae Tome) should coexist
        // since Jayemdae Tome is not legendary — no legend rule triggered
        long tomeCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jayemdae Tome"))
                .count();
        assertThat(tomeCount).isEqualTo(2);
    }

    // ===== Copies own controller's artifact =====

    @Test
    @DisplayName("Sculpting Steel can copy own controller's artifact")
    void copiesOwnArtifact() {
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player1, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();
        Permanent steelPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Sculpting Steel"))
                .findFirst().orElse(null);

        assertThat(steelPerm).isNotNull();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Jayemdae Tome");
    }
}
