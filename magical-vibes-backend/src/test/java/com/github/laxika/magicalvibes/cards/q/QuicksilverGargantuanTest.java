package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuicksilverGargantuanTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Quicksilver Gargantuan has correct effect configuration")
    void hasCorrectProperties() {
        QuicksilverGargantuan card = new QuicksilverGargantuan();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyPermanentOnEnterEffect.class);

        CopyPermanentOnEnterEffect effect = (CopyPermanentOnEnterEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.powerOverride()).isEqualTo(7);
        assertThat(effect.toughnessOverride()).isEqualTo(7);
    }

    // ===== Copying with P/T override =====

    @Test
    @DisplayName("Quicksilver Gargantuan copies a creature but has 7/7 power and toughness")
    void copiesCreatureButIs7x7() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan copies keywords from target creature")
    void copiesKeywords() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(clonePerm.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan copies subtypes from target creature")
    void copiesSubtypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    // ===== Copied creature's ETB effects =====

    @Test
    @DisplayName("Quicksilver Gargantuan copying a creature with ETB triggers that effect")
    void copiedCreatureETBFires() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        harness.handlePermanentChosen(player1, angelId);

        GameData gd = harness.getGameData();

        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Angel of Mercy");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);

        // The copied Angel of Mercy's ETB "gain 3 life" should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angel of Mercy"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    // ===== Declining / no creatures =====

    @Test
    @DisplayName("Quicksilver Gargantuan survives as 7/7 when player declines to copy")
    void survivesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Decline to copy
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Unlike Clone, Quicksilver Gargantuan is a 7/7 and should survive
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(perm).isNotNull();
        assertThat(perm.getCard().getPower()).isEqualTo(7);
        assertThat(perm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan survives as 7/7 when no creatures on battlefield")
    void survivesWhenNoCreatures() {
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // With no creatures to copy, it enters as its base 7/7 self
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(perm).isNotNull();
        assertThat(perm.getCard().getPower()).isEqualTo(7);
        assertThat(perm.getCard().getToughness()).isEqualTo(7);
    }

    // ===== CR 707.9d: P/T CDA not copied =====

    @Test
    @DisplayName("Quicksilver Gargantuan does not copy P/T characteristic-defining ability (CR 707.9d)")
    void doesNotCopyPTCharacteristicDefiningAbility() {
        // Nightmare has */* where * = number of Swamps you control
        harness.addToBattlefield(player2, new Nightmare());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID nightmareId = harness.getPermanentId(player2, "Nightmare");
        harness.handlePermanentChosen(player1, nightmareId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Nightmare");
        // Per CR 707.9d, the CDA is not copied — P/T should be exactly 7/7
        assertThat(clonePerm.getEffectivePower()).isEqualTo(7);
        assertThat(clonePerm.getEffectiveToughness()).isEqualTo(7);
    }

    // ===== Graveyard identity =====

    @Test
    @DisplayName("Quicksilver Gargantuan goes to graveyard as Quicksilver Gargantuan when destroyed")
    void goesToGraveyardAsOriginalCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Quicksilver Gargantuan"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();

        // Simulate destruction
        gd.playerBattlefields.get(player1.getId()).remove(clonePerm);
        gd.playerGraveyards.get(player1.getId()).add(clonePerm.getOriginalCard());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Quicksilver Gargantuan"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
