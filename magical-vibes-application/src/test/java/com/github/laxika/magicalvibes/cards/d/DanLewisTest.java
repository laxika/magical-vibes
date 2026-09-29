package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TitanForge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanLewis.class, TitanForge.class, GrizzlyBears.class, LeoninScimitar.class, IronMyr.class})
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
}
