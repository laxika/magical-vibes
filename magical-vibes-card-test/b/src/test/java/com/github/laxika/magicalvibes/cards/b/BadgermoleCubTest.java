package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BadgermoleCub.class, ElvishMystic.class, Forest.class, TurnToFrog.class})
class BadgermoleCubTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void earthbendsLandOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds an additional green mana when you tap a creature for mana")
    void creatureTapProducesAdditionalGreen() {
        harness.addToBattlefield(player1, new BadgermoleCub());
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        mystic.setSummoningSick(false);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot earthbend a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new BadgermoleCub(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId())
                .doesNotContain(opponentLand.getId());
    }

    @Test
    void earthbendedLandProducesBonusManaImmediately() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ordinaryLandDoesNotProduceBonusMana() {
        harness.addToBattlefield(player1, new BadgermoleCub());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void opponentsCreatureDoesNotProduceBonusMana() {
        harness.addToBattlefield(player1, new BadgermoleCub());
        Permanent mystic = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());
        mystic.setSummoningSick(false);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void multipleCubsEachProduceBonusMana() {
        harness.addToBattlefield(player1, new BadgermoleCub());
        harness.addToBattlefield(player1, new BadgermoleCub());
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        mystic.setSummoningSick(false);

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void earthbendedLandReturnsTappedAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedLand(land);
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedLand(land);
    }

    @Test
    void cubWithNoAbilitiesDoesNotProduceBonusMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());
        Permanent cub = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof BadgermoleCub).findFirst().orElseThrow();
        turnToFrog(cub.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void earthbendReturnSurvivesLandLosingAbilities() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermoleCub(land.getId());
        turnToFrog(land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertReturnedLand(land);
    }

    private void turnToFrog(UUID targetId) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void assertReturnedLand(Permanent original) {
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(original.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    private void castBadgermoleCub(UUID targetId) {
        harness.castFromHand(player1, new BadgermoleCub(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

}
