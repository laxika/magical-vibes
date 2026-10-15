package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({DwarvenShortsword.class, GrizzlyBears.class, OrdinaryBear.class})
class DwarvenShortswordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Dwarven Shortsword creates and equips a red Dwarf token")
    void enteringCreatesAndEquipsDwarf() {
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shortsword = findPermanent(player1, "Dwarven Shortsword");
        Permanent dwarf = findPermanent(player1, "Dwarf");
        assertThat(dwarf.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dwarf.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF);
        assertThat(shortsword.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip {2} attaches Dwarven Shortsword and gives +1/+2")
    void equipAttachesAndBoostsCreature() {
        Permanent shortsword = addCreatureReady(player1, new DwarvenShortsword());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(shortsword.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Re-equipping moves the bonus from the created Dwarf only on resolution")
    void reEquippingMovesBonusOnResolution() {
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent shortsword = findPermanent(player1, "Dwarven Shortsword");
        Permanent dwarf = findPermanent(player1, "Dwarf");
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(shortsword.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(shortsword.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger still creates a Dwarf after the Shortsword leaves")
    void entryTriggerCreatesTokenWithoutEquipment() {
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent shortsword = findPermanent(player1, "Dwarven Shortsword");
        assertThat(countPermanents(player1, "Dwarf")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(shortsword);
        gd.playerGraveyards.get(player1.getId()).add(shortsword.getCard());

        resolveAllTriggers();

        Permanent dwarf = findPermanent(player1, "Dwarf");
        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent shortsword = harness.addToBattlefieldAndReturn(player1, new DwarvenShortsword());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(shortsword.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new DwarvenShortsword());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal re-equip target leaves the original attachment intact")
    void failedReEquipPreservesAttachment() {
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent shortsword = findPermanent(player1, "Dwarven Shortsword");
        Permanent dwarf = findPermanent(player1, "Dwarf");
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());

        harness.passBothPriorities();

        assertThat(shortsword.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
