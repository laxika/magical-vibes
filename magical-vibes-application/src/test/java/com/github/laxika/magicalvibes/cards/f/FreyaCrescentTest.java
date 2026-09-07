package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
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

@CardUsed({FreyaCrescent.class, VulshokMorningstar.class, CopperMyr.class, GrizzlyBears.class})
class FreyaCrescentTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void flyingIsLimitedToControllerTurn() {
        Permanent freya = addCreatureReady(player1, new FreyaCrescent());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, freya, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, freya, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Restricted red mana casts an Equipment spell")
    void restrictedManaCastsEquipmentSpell() {
        Permanent freya = addCreatureReady(player1, new FreyaCrescent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(freya), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new VulshokMorningstar()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted red mana cannot cast a non-Equipment artifact")
    void restrictedManaCannotCastNonEquipmentArtifact() {
        Permanent freya = addCreatureReady(player1, new FreyaCrescent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(freya), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CopperMyr()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Restricted red mana pays for an equip ability")
    void restrictedManaPaysForEquipAbility() {
        Permanent freya = addCreatureReady(player1, new FreyaCrescent());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(freya), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(equipment), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
