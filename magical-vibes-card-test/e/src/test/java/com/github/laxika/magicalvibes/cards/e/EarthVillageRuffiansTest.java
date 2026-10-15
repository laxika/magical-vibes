package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.z.ZukosExile;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthVillageRuffians.class, Forest.class, Murder.class, ZukosExile.class})
class EarthVillageRuffiansTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, earthbends a land you control")
    void earthbendsLandWhenItDies() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        destroy(ruffians);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its death trigger cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        destroy(ruffians);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(ownLand.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).doesNotContain(opponentLand.getId());
    }

    @Test
    @DisplayName("An earthbended land returns tapped without animation or counters after dying")
    void landReturnsAfterDying() {
        Permanent land = earthbendForest();

        destroy(land);
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedForest(land);
    }

    @Test
    @DisplayName("An earthbended land returns tapped after being exiled")
    void landReturnsAfterExile() {
        Permanent land = earthbendForest();
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getCard().getId())).isNotNull();
        harness.passBothPriorities();

        assertReturnedForest(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Earthbending an already earthbended land adds counters")
    void repeatedEarthbendAddsCounters() {
        Permanent land = earthbendForest();
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());

        destroy(ruffians);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    @Test
    @DisplayName("Dying without a land to target leaves no unresolved trigger")
    void diesWithoutControlledLand() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        harness.addToBattlefield(player2, new Forest());

        destroy(ruffians);

        harness.assertInGraveyard(player1, "Earth Village Ruffians");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent earthbendForest() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        destroy(ruffians);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        return land;
    }

    private void assertReturnedForest(Permanent oldLand) {
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned.getId()).isNotEqualTo(oldLand.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    private void destroy(Permanent permanent) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
    }
}
