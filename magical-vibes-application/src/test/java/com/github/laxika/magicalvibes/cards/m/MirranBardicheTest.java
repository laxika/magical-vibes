package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirranBardiche.class, GrizzlyBears.class})
class MirranBardicheTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Mirran Bardiche creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new MirranBardiche()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bardiche = findPermanent(player1, "Mirran Bardiche");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(bardiche.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equip moves Mirran Bardiche and its bonus to another creature")
    void equipMovesBardicheAndBonus() {
        harness.setHand(player1, List.of(new MirranBardiche()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent bardiche = findPermanent(player1, "Mirran Bardiche");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(bardiche.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("For Mirrodin still creates a Rebel if the Equipment leaves before resolution")
    void createsRebelAfterEquipmentLeaves() {
        harness.setHand(player1, List.of(new MirranBardiche()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bardiche = findPermanent(player1, "Mirran Bardiche");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bardiche));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mirran Bardiche");
        harness.assertInGraveyard(player1, "Mirran Bardiche");
        assertThat(countPermanents(player1, "Rebel")).isEqualTo(1);
        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Each Mirran Bardiche attaches to its own newly created Rebel")
    void eachEquipmentAttachesToItsOwnRebel() {
        harness.setHand(player1, List.of(new MirranBardiche(), new MirranBardiche()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> equipment = findPermanents(player1, "Mirran Bardiche");
        List<Permanent> rebels = findPermanents(player1, "Rebel");
        assertThat(equipment).hasSize(2);
        assertThat(rebels).hasSize(2);
        assertThat(equipment.get(0).getAttachedTo()).isEqualTo(rebels.get(0).getId());
        assertThat(equipment.get(1).getAttachedTo()).isEqualTo(rebels.get(1).getId());
        for (Permanent rebel : rebels) {
            assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, rebel, Keyword.VIGILANCE)).isTrue();
        }
    }
}
