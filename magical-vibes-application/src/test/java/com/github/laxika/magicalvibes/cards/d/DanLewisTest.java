package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.TitanForge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanLewis.class, TitanForge.class, GrizzlyBears.class, LeoninScimitar.class, IronMyr.class,
        MindStone.class, SolemnSimulacrum.class, SwordsToPlowshares.class})
class DanLewisTest extends BaseCardTest {

    @Test
    void controlledNoncreatureArtifactBecomesEquipmentAndCanEquip() {
        harness.addToBattlefield(player1, new DanLewis());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.computeStaticBonus(gd, forge).grantedSubtypes()).contains(CardSubtype.EQUIPMENT);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        // Titan Forge's two native abilities come before Dan Lewis's granted equip ability.
        harness.activateAbility(player1, 1, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(forge.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    void creaturesAndExistingEquipmentAreNotAffected() {
        harness.addToBattlefield(player1, new DanLewis());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.permanentHasSubtype(myr, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, myr).grantedActivatedAbilities()).isEmpty();

        scimitar.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    void doesNotAffectOpponentsArtifacts() {
        harness.addToBattlefield(player1, new DanLewis());
        Permanent opponentForge = harness.addToBattlefieldAndReturn(player2, new TitanForge());

        assertThat(gqs.permanentHasSubtype(opponentForge, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, opponentForge).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    void equippedArtifactRetainsItsManaAbilityAndCanEquipWhileTapped() {
        Permanent dan = harness.addToBattlefieldAndReturn(player1, new DanLewis());
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        stone.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 2, null, dan.getId());
        harness.passBothPriorities();

        assertThat(stone.getAttachedTo()).isEqualTo(dan.getId());
        assertThat(gqs.getEffectivePower(gd, dan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dan)).isEqualTo(2);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(stone.isTapped()).isTrue();
        assertThat(stone.getAttachedTo()).isEqualTo(dan.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.activateAbility(player1, 1, 2, null, dan.getId());
        harness.passBothPriorities();

        assertThat(stone.isTapped()).isTrue();
        assertThat(stone.getAttachedTo()).isEqualTo(dan.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.getEffectivePower(gd, dan)).isEqualTo(3);
    }

    @Test
    void multipleArtifactsEachGrantOnePowerWithoutIncreasingToughness() {
        Permanent dan = harness.addToBattlefieldAndReturn(player1, new DanLewis());
        Permanent firstStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent secondStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 2, null, dan.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, 2, null, dan.getId());
        harness.passBothPriorities();

        assertThat(firstStone.getAttachedTo()).isEqualTo(dan.getId());
        assertThat(secondStone.getAttachedTo()).isEqualTo(dan.getId());
        assertThat(gqs.getEffectivePower(gd, dan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dan)).isEqualTo(2);
    }

    @Test
    void artifactBecomesUnattachedAndStopsBoostingWhenDanLeaves() {
        Permanent dan = harness.addToBattlefieldAndReturn(player1, new DanLewis());
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent simulacrum = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 2, null, simulacrum.getId());
        harness.passBothPriorities();
        assertThat(stone.getAttachedTo()).isEqualTo(simulacrum.getId());
        assertThat(gqs.getEffectivePower(gd, simulacrum)).isEqualTo(3);

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, dan.getId());

        assertThat(stone.getAttachedTo()).isNull();
        assertThat(gqs.hasEffectiveSubtype(gd, stone, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, simulacrum)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, simulacrum)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Mind Stone");
    }
}
