package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetterOffer.class, Forest.class, GrizzlyBears.class, HillGiant.class, Maro.class, Shock.class, Unsummon.class})
class BetterOfferTest extends BaseCardTest {

    @Test
    void putsAnEligibleRandomCreatureOntoTheBattlefieldAsXOverXWithWard() {
        Forest forest = new Forest();
        HillGiant hillGiant = new HillGiant();
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player2, List.of(forest, hillGiant, eligible));
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        Permanent stolen = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, stolen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stolen)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stolen, Keyword.WARD)).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest, hillGiant);
    }

    @Test
    void grantedWardCountersAnOpponentsLethalSpellWhenTheyCannotPay() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player2, List.of(eligible));
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        Permanent stolen = findPermanent(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, stolen.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen);
    }

    @Test
    void canOnlyTargetAnOpponent() {
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basePowerAndToughnessOverrideACharacteristicDefiningAbility() {
        harness.setLibrary(player2, List.of(new Maro()));
        harness.setHand(player1, List.of(new BetterOffer(), new Forest()));
        addBetterOfferMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        Permanent stolen = findPermanent(player1, "Maro");
        assertThat(gqs.getEffectivePower(gd, stolen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stolen)).isEqualTo(4);
    }

    @Test
    void doesNothingWhenTheLibraryHasNoEligibleCreature() {
        Forest forest = new Forest();
        HillGiant giant = new HillGiant();
        harness.setLibrary(player2, List.of(forest, giant));
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest, giant);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Better Offer");
    }

    @Test
    void resolvesWithoutAnEffectWithAnEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Better Offer");
    }

    @Test
    void perpetualStatsAndWardSurviveReturningToTheOwnersHandAndRecasting() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BetterOffer()));
        addBetterOfferMana();
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        Permanent stolen = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, stolen.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent recast = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(3);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, recast.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unsummon");
    }

    private void addBetterOfferMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
