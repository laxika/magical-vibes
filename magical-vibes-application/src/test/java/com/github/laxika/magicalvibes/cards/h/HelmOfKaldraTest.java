package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NemesisMask;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.cards.s.ShieldOfKaldra;
import com.github.laxika.magicalvibes.cards.s.SwordOfKaldra;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfKaldra.class, ShieldOfKaldra.class, SwordOfKaldra.class,
        MyrMoonvessel.class, NemesisMask.class})
class HelmOfKaldraTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike, trample, and haste")
    void equippedCreatureHasKeywords() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();

        helm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        helm.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability does nothing unless all three Kaldra Equipment are controlled")
    void abilityRequiresAllKaldraEquipment() {
        harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        harness.addToBattlefieldAndReturn(player1, new SwordOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kaldra")).isEmpty();
    }

    @Test
    @DisplayName("Equipment controlled by the opponent does not satisfy the Kaldra requirement")
    void abilityRequiresKaldraEquipmentUnderOneController() {
        harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        harness.addToBattlefieldAndReturn(player1, new SwordOfKaldra());
        harness.addToBattlefieldAndReturn(player2, new ShieldOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kaldra")).isEmpty();
    }

    @Test
    @DisplayName("The Kaldra condition is checked as the ability resolves")
    void abilityRechecksRequirementOnResolution() {
        harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        harness.addToBattlefieldAndReturn(player1, new SwordOfKaldra());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(shield);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kaldra")).isEmpty();
    }

    @Test
    @DisplayName("Creates legendary Kaldra and attaches only the three Kaldra Equipment")
    void createsAndEquipsKaldra() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfKaldra());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfKaldra());
        Permanent existingHost = addCreatureReady(player1, new MyrMoonvessel());
        sword.setAttachedTo(existingHost.getId());
        Permanent unrelatedEquipment = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        Permanent opponentSword = harness.addToBattlefieldAndReturn(player2, new SwordOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var kaldraPermanents = findPermanents(player1, "Kaldra");
        assertThat(kaldraPermanents).hasSize(1);
        Permanent kaldra = kaldraPermanents.getFirst();
        assertThat(kaldra.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(kaldra.getCard().getColors()).isEmpty();
        assertThat(kaldra.getCard().isToken()).isTrue();
        assertThat(kaldra.getCard().getPower()).isEqualTo(4);
        assertThat(kaldra.getCard().getToughness()).isEqualTo(4);
        assertThat(kaldra.getCard().getSubtypes()).containsExactly(CardSubtype.AVATAR);
        assertThat(kaldra.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(helm.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(sword.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(shield.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(unrelatedEquipment.getAttachedTo()).isNull();
        assertThat(opponentSword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {2} attaches Helm of Kaldra to a creature you control")
    void equipAttachesToCreature() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipRequiresCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new HelmOfKaldra());
        Permanent opponentCreature = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
