package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoninShadowStalker.class, VulshokMorningstar.class, CopperMyr.class, GrizzlyBears.class})
class RoninShadowStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life for two mana of one color and can do so only once each turn")
    void paysLifeForRestrictedManaOnceEachTurn() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana casts an Equipment spell")
    void restrictedManaCastsEquipment() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.setHand(player1, List.of(new VulshokMorningstar()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-Equipment artifact")
    void restrictedManaCannotCastNonEquipment() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.setHand(player1, List.of(new CopperMyr()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Restricted mana pays for an equip ability")
    void restrictedManaPaysForEquipAbility() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.activateAbility(player1, battlefieldIndex(equipment), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing an attached Equipment gives a creature -4/-4")
    void sacrificesAttachedEquipmentToGiveCreatureMinusFourMinusFour() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
