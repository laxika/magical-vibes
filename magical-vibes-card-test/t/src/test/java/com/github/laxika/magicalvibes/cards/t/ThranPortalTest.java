package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SquirrelNest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranPortal.class, Forest.class, SquirrelNest.class})
class ThranPortalTest extends BaseCardTest {

    @Test
    void entersUntappedWithTwoOrFewerOtherLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        playPortal();

        harness.handleListChoice(player1, "ISLAND");

        Permanent portal = findPermanent(player1, "Thran Portal");
        assertThat(portal.isTapped()).isFalse();
        assertThat(gqs.effectiveBasicLandTypes(gd, portal)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        playPortal();

        harness.handleListChoice(player1, "MOUNTAIN");

        Permanent portal = findPermanent(player1, "Thran Portal");
        assertThat(portal.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, portal)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    void chosenTypeProducesManaForOneLife() {
        Permanent portal = addReadyPortal(CardSubtype.SWAMP);
        harness.setLife(player1, 5);

        harness.tapPermanent(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(portal.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutLifeToPay() {
        Permanent portal = addReadyPortal(CardSubtype.FOREST);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(portal.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void chosenTypePreservesGate() {
        playPortal();
        harness.handleListChoice(player1, "PLAINS");

        Permanent portal = findPermanent(player1, "Thran Portal");
        assertThat(gqs.effectiveLandTypes(gd, portal))
                .containsExactlyInAnyOrder(CardSubtype.GATE, CardSubtype.PLAINS);
    }

    @Test
    void opponentLandsDoNotMakePortalEnterTapped() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        playPortal();
        harness.handleListChoice(player1, "FOREST");

        assertThat(findPermanent(player1, "Thran Portal").isTapped()).isFalse();
    }

    @Test
    void manaFromPlayedPortalCostsLifeImmediatelyWithoutUsingStack() {
        playPortal();
        harness.handleListChoice(player1, "ISLAND");
        harness.setLife(player1, 5);

        harness.tapPermanent(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedNonManaAbilityDoesNotCostLifeWithoutChosenType() {
        Permanent portal = addReadyPortal(null);
        harness.setHand(player1, List.of(new SquirrelNest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, portal.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(portal.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Squirrel"));
    }

    private void playPortal() {
        harness.setHand(player1, List.of(new ThranPortal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyPortal(CardSubtype chosenSubtype) {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new ThranPortal());
        portal.setChosenSubtype(chosenSubtype);
        portal.setSummoningSick(false);
        return portal;
    }
}
