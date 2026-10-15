package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GorgonsHead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DalakosCrafterOfWonders.class, GorgonsHead.class, GrizzlyBears.class, CopperMyr.class,
        IcyManipulator.class})
class DalakosCrafterOfWondersTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds two colorless mana restricted to artifacts")
    void addsRestrictedColorlessMana() {
        Permanent dalakos = addReadyDalakos();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(dalakos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact spell")
    void restrictedManaPaysForArtifactSpell() {
        addReadyDalakos();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact ability")
    void restrictedManaPaysForArtifactAbility() {
        addReadyDalakos();
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icyManipulator.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a non-artifact spell")
    void restrictedManaCannotPayForNonArtifactSpell() {
        addReadyDalakos();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Equipped creatures you control have flying and haste")
    void equippedCreaturesYouControlHaveFlyingAndHaste() {
        addReadyDalakos();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent gorgonsHead = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        gorgonsHead.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Dalakos gets flying and haste when equipped")
    void equippedDalakosGetsFlyingAndHaste() {
        Permanent dalakos = addReadyDalakos();
        Permanent gorgonsHead = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        gorgonsHead.setAttachedTo(dalakos.getId());

        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Dalakos does not grant keywords to unequipped or opposing creatures")
    void doesNotGrantKeywordsToUnequippedOrOpposingCreatures() {
        addReadyDalakos();
        Permanent unequippedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gorgonsHead = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        gorgonsHead.setAttachedTo(opposingCreature.getId());

        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Restricted mana pays the entire equip cost")
    void restrictedManaPaysEquipCost() {
        Permanent dalakos = addReadyDalakos();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, dalakos.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(dalakos.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped summoning-sick Dalakos cannot tap for mana")
    void unequippedSummoningSickDalakosCannotTapForMana() {
        harness.addToBattlefield(player1, new DalakosCrafterOfWonders());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Equipped summoning-sick Dalakos can tap for mana immediately")
    void equippedSummoningSickDalakosCanTapForMana() {
        Permanent dalakos = harness.addToBattlefieldAndReturn(player1, new DalakosCrafterOfWonders());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        equipment.setAttachedTo(dalakos.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(dalakos.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Moving the last Equipment removes flying and haste from its former creature")
    void movingEquipmentUpdatesGrantedKeywords() {
        Permanent dalakos = addReadyDalakos();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        equipment.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.activateAbility(player1, 2, 0, null, dalakos.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Equipment still makes your creature equipped")
    void opposingEquipmentGrantsKeywordsToYourCreature() {
        Permanent dalakos = addReadyDalakos();
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new GorgonsHead());
        equipment.setAttachedTo(dalakos.getId());

        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalakos, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyDalakos() {
        return addCreatureReady(player1, new DalakosCrafterOfWonders());
    }
}
