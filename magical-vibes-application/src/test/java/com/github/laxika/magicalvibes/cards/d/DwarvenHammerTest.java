package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenHammer.class, AxgardCavalry.class})
class DwarvenHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips a Dwarf Berserker")
    void payingEtbCostCreatesAndEquipsDwarfBerserker() {
        Permanent hammer = castHammerWithMana(4);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent dwarfBerserker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(hammer.getAttachedTo()).isEqualTo(dwarfBerserker.getId());
        assertThat(gqs.getEffectivePower(gd, dwarfBerserker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dwarfBerserker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dwarfBerserker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Dwarf Berserker")
    void decliningEtbCostCreatesNoDwarfBerserker() {
        Permanent hammer = castHammerWithMana(4);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
        assertThat(hammer.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Dwarven Hammer to a creature you control")
    void equipAttachesHammer() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger attaches only the Hammer that entered")
    void etbDoesNotAttachAnotherHammer() {
        Permanent otherHammer = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        Permanent hammer = castHammerWithMana(4);

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(hammer.getAttachedTo()).isEqualTo(token.getId());
        assertThat(otherHammer.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("Re-equipping removes the bonus and trample from the token")
    void reEquippingMovesBonusesToNewCreature() {
        Permanent hammer = castHammerWithMana(4);
        harness.handleMayAbilityChosen(player1, true);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castHammerWithMana(int colorless) {
        java.util.Set<java.util.UUID> existingIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());
        harness.castFromHand(player1, new DwarvenHammer(), "{2}{R}");
        harness.addMana(player1, ManaColor.COLORLESS, colorless - 2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DwarvenHammer)
                .filter(permanent -> !existingIds.contains(permanent.getId()))
                .findFirst()
                .orElseThrow();
    }
}
