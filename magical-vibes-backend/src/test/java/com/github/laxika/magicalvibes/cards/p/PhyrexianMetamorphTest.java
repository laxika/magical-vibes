package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianMetamorphTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Metamorph has CopyPermanentOnEnterEffect")
    void hasCorrectProperties() {
        PhyrexianMetamorph card = new PhyrexianMetamorph();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyPermanentOnEnterEffect.class);
    }

    // ===== Copying a creature — should gain artifact type =====

    @Test
    @DisplayName("Copying a creature gains artifact type in addition to creature")
    void copyingCreatureGainsArtifactType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(metamorphPerm.getCard().getPower()).isEqualTo(2);
        assertThat(metamorphPerm.getCard().getToughness()).isEqualTo(2);
        // Primary type is creature, but artifact should be added as an additional type
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    // ===== Copying a non-creature artifact — already artifact =====

    @Test
    @DisplayName("Copying a non-creature artifact is NOT a creature (just an artifact)")
    void copyingNonCreatureArtifactIsNotCreature() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Jayemdae Tome");
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        // Per rules: copying a noncreature artifact means Metamorph is NOT a creature
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).doesNotContain(CardType.CREATURE);
        assertThat(metamorphPerm.getCard().getActivatedAbilities()).hasSize(1);
    }

    // ===== Copying an artifact creature — already has both types =====

    @Test
    @DisplayName("Copying an artifact creature preserves both types")
    void copyingArtifactCreaturePreservesBothTypes() {
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID juggernautId = harness.getPermanentId(player2, "Juggernaut");
        harness.handlePermanentChosen(player1, juggernautId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Juggernaut");
        assertThat(metamorphPerm.getCard().getPower()).isEqualTo(5);
        assertThat(metamorphPerm.getCard().getToughness()).isEqualTo(3);
        // Juggernaut is already "Artifact Creature" — types should be preserved
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.CREATURE);
    }

    // ===== Copies keywords =====

    @Test
    @DisplayName("Copying a creature with flying copies the keyword and adds artifact type")
    void copiesKeywordsAndAddsArtifact() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(metamorphPerm.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    // ===== Declining / no valid targets =====

    @Test
    @DisplayName("Enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phyrexian Metamorph"));
    }

    @Test
    @DisplayName("Enters as 0/0 and dies when no artifacts or creatures on battlefield")
    void diesWhenNoValidTargets() {
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phyrexian Metamorph"));
    }

    // ===== Graveyard identity =====

    @Test
    @DisplayName("Goes to graveyard as Phyrexian Metamorph when destroyed")
    void goesToGraveyardAsPhyrexianMetamorph() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);
        assertThat(metamorphPerm).isNotNull();

        gd.playerBattlefields.get(player1.getId()).remove(metamorphPerm);
        gd.playerGraveyards.get(player1.getId()).add(metamorphPerm.getOriginalCard());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phyrexian Metamorph"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
