package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicArmaments.class, BorderlandRanger.class})
class AngelicArmamentsTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches the Equipment to a creature you control")
    void equipAttachesToCreature() {
        harness.addToBattlefield(player1, new AngelicArmaments());
        harness.addToBattlefield(player1, new BorderlandRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player1, "Borderland Ranger");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent armor = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(armor.getAttachedTo()).isEqualTo(creatureId);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and has flying")
    void equippedCreatureGetsBoostAndFlying() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature becomes white in addition to its other colors")
    void equippedCreatureBecomesWhiteAdditively() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        assertThat(gqs.hasColor(gd, creature, CardColor.WHITE)).isFalse();

        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasColor(gd, creature, CardColor.WHITE)).isTrue();
        // Green (its printed colour) is retained — the grant is additive
        assertThat(gqs.getEffectiveColors(gd, creature)).contains(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    @DisplayName("Equipped creature becomes an Angel in addition to its other types")
    void equippedCreatureBecomesAngel() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.SCOUT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.RANGER)).isTrue();
    }

    @Test
    @DisplayName("All grants wear off when the Equipment is unattached")
    void grantsRemovedWhenUnequipped() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        armor.setAttachedTo(creature.getId());
        armor.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, creature, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ANGEL)).isFalse();
    }

    @Test
    @DisplayName("Re-equipping transfers all grants to the new creature")
    void reequipTransfersAllGrants() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(armor.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, first)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.SCOUT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.RANGER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, second)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.SCOUT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.RANGER)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorderlandRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature")
    void cannotEquipNoncreature() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armor.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip requires all four mana")
    void cannotEquipWithInsufficientMana() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
