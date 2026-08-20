package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castHammerWithMana(int colorless) {
        harness.setHand(player1, List.of(new DwarvenHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DwarvenHammer)
                .findFirst()
                .orElseThrow();
    }
}
