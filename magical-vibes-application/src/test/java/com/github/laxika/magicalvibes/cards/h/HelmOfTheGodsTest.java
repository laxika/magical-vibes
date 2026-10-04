package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfTheGods.class, GrizzlyBears.class, AuraOfSilence.class})
class HelmOfTheGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each enchantment the Helm's controller controls")
    void boostsPerEnchantment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());

        // 2/2 base + 2 enchantments = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost updates dynamically as enchantments enter and leave")
    void updatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new AuraOfSilence());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Aura of Silence"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count the opponent's enchantments")
    void ignoresOpponentEnchantments() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.addToBattlefield(player2, new AuraOfSilence());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {1} attaches the Helm to a creature you control")
    void equipForOne() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.activateAbility(player1, battlefield.indexOf(helm), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void failedReEquipLeavesOriginalCreatureEquipped() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(first.getId());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsEquipmentControllersEnchantmentsAfterCreatureChangesControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AuraOfSilence());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void reEquippingMovesBoostOnlyOnResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheGods());
        helm.setAttachedTo(first.getId());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, 0, null, second.getId());
        assertThat(helm.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new HelmOfTheGods());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HelmOfTheGods());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
