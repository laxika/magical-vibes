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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarforgedSword.class, GrizzlyBears.class, SuntailHawk.class})
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
    void promisedGiftCanTargetOnlyYourCreature() {
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castArtifactWithGift(player1, 0, opponentBear.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(UUID targetId, boolean giftPromised) {
        prepareCast();
        harness.castArtifactWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new StarforgedSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
