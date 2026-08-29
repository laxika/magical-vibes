package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AltarOfShadows;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheFloodMaw.class, AltarOfShadows.class, GrizzlyBears.class, Plains.class})
class IntoTheFloodMawTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsTargetCreatureToItsOwnersHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(bear, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Fish")).isNull();
    }

    @Test
    void withGiftReturnsTargetNonlandPermanentAndCreatesTappedFish() {
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new AltarOfShadows());

        cast(altar, true);

        harness.assertNotOnBattlefield(player2, "Altar of Shadows");
        harness.assertInHand(player2, "Altar of Shadows");
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    void withoutGiftCannotTargetNoncreaturePermanent() {
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new AltarOfShadows());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, altar.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void withGiftCannotTargetLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, plains.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent an opponent controls");
    }

    private void cast(Permanent target, boolean giftPromised) {
        prepareSpell();
        harness.castInstantWithGift(player1, 0, target.getId(), giftPromised);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new IntoTheFloodMaw()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
