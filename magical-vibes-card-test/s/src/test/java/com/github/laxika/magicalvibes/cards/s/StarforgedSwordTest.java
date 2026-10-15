package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarforgedSword.class, GrizzlyBears.class, SuntailHawk.class, Savor.class})
class StarforgedSwordTest extends BaseCardTest {

    @Test
    void withoutGiftDoesNotAttachOrCreateFish() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        cast(null, false);

        Permanent sword = findPermanent(player1, "Starforged Sword");
        assertThat(sword.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(findPermanents(player2, "Fish")).isEmpty();
    }

    @Test
    void promisedGiftCreatesTappedFishAndAttachesSword() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        cast(bear.getId(), true);

        Permanent sword = findPermanent(player1, "Starforged Sword");
        assertThat(sword.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    void equippedCreatureGetsBoostAndLosesFlying() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new StarforgedSword());
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(hawk.getId());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();
    }

    @Test
    void canPromiseGiftWithoutControllingACreature() {
        prepareCast();

        harness.castArtifactWithGift(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Starforged Sword").getAttachedTo()).isNull();
        assertThat(findPermanents(player2, "Fish")).hasSize(1);
        assertThat(findPermanent(player2, "Fish").isTapped()).isTrue();
    }

    @Test
    void giftStillCreatesFishWhenAttachmentTargetDies() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        prepareCast();
        harness.castArtifactWithGift(player1, 0, null, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hawk.getId());

        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, hawk.getId());
        harness.assertInGraveyard(player1, "Suntail Hawk");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Starforged Sword").getAttachedTo()).isNull();
        assertThat(findPermanents(player2, "Fish")).hasSize(1);
        assertThat(findPermanent(player2, "Fish").isTapped()).isTrue();
    }

    @Test
    void movingEquipmentRestoresPreviousCreaturesFlyingAndStats() {
        harness.addToBattlefield(player1, new StarforgedSword());
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    private void cast(UUID targetId, boolean giftPromised) {
        prepareCast();
        harness.castArtifactWithGift(player1, 0, null, giftPromised);
        harness.passBothPriorities();
        if (giftPromised) {
            harness.handlePermanentChosen(player1, targetId);
        }
        resolveAllTriggers();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new StarforgedSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
