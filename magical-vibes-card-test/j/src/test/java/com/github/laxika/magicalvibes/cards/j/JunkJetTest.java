package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunkJet.class, GrizzlyBears.class, Spellbook.class, Shatter.class, Mountain.class})
class JunkJetTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Junk token")
    void entersWithJunkToken() {
        harness.setHand(player1, List.of(new JunkJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getSubtypes()).contains(CardSubtype.JUNK);
        assertThat(junk.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another artifact doubles the equipped creature's power")
    void sacrificesArtifactAndDoublesEquippedCreaturePower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent jet = addCreatureReady(player1, new JunkJet());
        jet.setAttachedTo(creature.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice Junk Jet itself for its activated ability")
    void requiresAnotherArtifact() {
        Permanent jet = addCreatureReady(player1, new JunkJet());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Equip ability attaches Junk Jet to a creature you control")
    void equipsToControlledCreature() {
        Permanent jet = addCreatureReady(player1, new JunkJet());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(jet), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jet.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void doublesPowerAfterEquipmentIsDestroyedInResponse() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent jet = harness.addToBattlefieldAndReturn(player1, new JunkJet());
        jet.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shatter()));

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, jet.getId());
        harness.assertInGraveyard(player1, "Junk Jet");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void doublesNegativePower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setPowerModifier(-3);
        Permanent jet = harness.addToBattlefieldAndReturn(player1, new JunkJet());
        jet.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void repeatedActivationsDoublePowerAtEachResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent jet = harness.addToBattlefieldAndReturn(player1, new JunkJet());
        jet.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Spellbook"));
        harness.ensurePriority(player1);
        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
    }

    @Test
    void junkCanBeSacrificedImmediatelyToExileAndCastTopCard() {
        Permanent junk = createJunk();
        JunkJet topCard = new JunkJet();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, battlefieldIndex(junk), 0, null, null);
        harness.assertNotOnBattlefield(player1, "Junk");
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Junk Jet")).isEqualTo(2);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void junkCannotBeActivatedOutsideSorceryTiming() {
        Permanent junk = createJunk();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(junk), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Junk");
    }

    @Test
    void junkPlayPermissionExpiresAtEndOfTurn() {
        Permanent junk = createJunk();
        JunkJet topCard = new JunkJet();
        harness.setLibrary(player1, List.of(topCard));
        harness.activateAbility(player1, battlefieldIndex(junk), 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    private Permanent createJunk() {
        harness.enterBattlefieldAndReturn(player1, new JunkJet());
        harness.passBothPriorities();
        return findPermanent(player1, "Junk");
    }

    @Test
    void junkAllowsPlayingAnExiledLand() {
        Permanent junk = createJunk();
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, battlefieldIndex(junk), 0, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void canSacrificeCreatedJunkToDoublePowerDuringCombat() {
        Permanent junk = createJunk();
        Permanent jet = findPermanent(player1, "Junk Jet");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jet.setAttachedTo(creature.getId());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(junk);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent jet = harness.addToBattlefieldAndReturn(player1, new JunkJet());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(jet), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jet.getAttachedTo()).isNull();
    }

    @Test
    void doublingUsesCreatureEquippedAtResolution() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent current = addCreatureReady(player1, new GrizzlyBears());
        Permanent jet = harness.addToBattlefieldAndReturn(player1, new JunkJet());
        jet.setAttachedTo(original.getId());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        jet.setAttachedTo(current.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, current)).isEqualTo(4);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
