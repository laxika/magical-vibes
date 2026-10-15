package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShorelineLooter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheFloodMaw.class, FountainportBell.class, ShorelineLooter.class, Plains.class})
class IntoTheFloodMawTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsTargetCreatureToItsOwnersHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());

        cast(bear, false);

        harness.assertNotOnBattlefield(player2, "Shoreline Looter");
        harness.assertInHand(player2, "Shoreline Looter");
        harness.assertNotOnBattlefield(player2, "Fish");
    }

    @Test
    void withGiftReturnsTargetNonlandPermanentAndCreatesTappedFish() {
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new FountainportBell());

        cast(altar, true);

        harness.assertNotOnBattlefield(player2, "Fountainport Bell");
        harness.assertInHand(player2, "Fountainport Bell");
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    void withoutGiftCannotTargetNoncreaturePermanent() {
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new FountainportBell());
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

    @Test
    void withGiftCanStillReturnCreature() {
        Permanent looter = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());

        cast(looter, true);

        harness.assertInHand(player2, "Shoreline Looter");
        harness.assertNotOnBattlefield(player2, "Shoreline Looter");
        assertThat(findPermanents(player2, "Fish")).hasSize(1);
        assertThat(findPermanent(player2, "Fish").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Fish");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotTargetOwnCreature(boolean giftPromised) {
        Permanent looter = harness.addToBattlefieldAndReturn(player1, new ShorelineLooter());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, looter.getId(), giftPromised))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an opponent controls");
    }

    @Test
    void withGiftCannotTargetOwnNoncreaturePermanent() {
        Permanent bell = harness.addToBattlefieldAndReturn(player1, new FountainportBell());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, bell.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent an opponent controls");
    }

    @Test
    void doesNotGiveGiftWhenTargetIsSacrificedInResponse() {
        Permanent bell = harness.addToBattlefieldAndReturn(player2, new FountainportBell());
        harness.setLibrary(player2, List.of(new Plains()));
        prepareSpell();
        harness.castInstantWithGift(player1, 0, bell.getId(), true);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountainport Bell");
        harness.assertNotInHand(player2, "Fountainport Bell");
        harness.assertInGraveyard(player1, "Into the Flood Maw");
        harness.assertNotOnBattlefield(player2, "Fish");
        harness.assertNotOnBattlefield(player1, "Fish");
    }

    @Test
    void returnsOpponentControlledCreatureToItsActualOwner() {
        Permanent looter = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        gd.stolenCreatures.put(looter.getId(), player1.getId());

        cast(looter, false);

        harness.assertInHand(player1, "Shoreline Looter");
        harness.assertNotInHand(player2, "Shoreline Looter");
        harness.assertNotOnBattlefield(player2, "Shoreline Looter");
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
