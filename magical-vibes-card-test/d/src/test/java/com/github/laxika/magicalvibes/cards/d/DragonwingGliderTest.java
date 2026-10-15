package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PredationSteward;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonwingGlider.class, PredationSteward.class})
class DragonwingGliderTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new DragonwingGlider()));
        addManaForDragonwingGlider();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent glider = findPermanent(player1, "Dragonwing Glider");

        assertThat(glider.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip moves Dragonwing Glider and its bonuses to another creature")
    void equipMovesGliderToAnotherCreature() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new DragonwingGlider());
        Permanent steward = addCreatureReady(player1, new PredationSteward());

        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, steward, Keyword.FLYING)).isFalse();

        addManaForDragonwingGlider();
        harness.activateAbility(player1, 0, null, steward.getId());
        harness.passBothPriorities();

        assertThat(glider.getAttachedTo()).isEqualTo(steward.getId());
        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, steward, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, steward, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Moving the Glider removes its bonuses from the original Rebel")
    void movingGliderRemovesRebelBonuses() {
        harness.setHand(player1, List.of(new DragonwingGlider()));
        addManaForDragonwingGlider();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent glider = findPermanent(player1, "Dragonwing Glider");
        Permanent steward = addCreatureReady(player1, new PredationSteward());

        addManaForDragonwingGlider();
        harness.activateAbility(player1, 0, null, steward.getId());
        harness.passBothPriorities();

        assertThat(glider.getAttachedTo()).isEqualTo(steward.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, steward, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, steward, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("For Mirrodin! still creates a Rebel if the Glider leaves before resolution")
    void createsRebelWhenEquipmentLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new DragonwingGlider()));
        addManaForDragonwingGlider();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent glider = findPermanent(player1, "Dragonwing Glider");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, glider));
        resolveAllTriggers();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(countPermanents(player1, "Rebel")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Dragonwing Glider");
    }

    @Test
    @DisplayName("Removing the Glider removes all granted bonuses from the Rebel")
    void removingGliderRemovesBonuses() {
        harness.setHand(player1, List.of(new DragonwingGlider()));
        addManaForDragonwingGlider();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent glider = findPermanent(player1, "Dragonwing Glider");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, glider));

        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isFalse();
    }

    private void addManaForDragonwingGlider() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
