package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
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

@CardUsed({ShelteringPrayers.class, Plains.class, WintermoonMesa.class})
class ShelteringPrayersTest extends BaseCardTest {

    @Test
    void grantsShroudToBasicLandsWhenTheirControllerHasThreeOrFewerLands() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent thirdPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, firstPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, thirdPlains, Keyword.SHROUD)).isTrue();
    }

    @Test
    void countsEachPlayersLandsIndependentlyAndAffectsBasicLandsOnly() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent ownBasicLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new WintermoonMesa());

        Permanent opponentBasicLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent opponentNonbasicLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());

        assertThat(gqs.hasKeyword(gd, ownBasicLand, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBasicLand, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentNonbasicLand, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudPreventsLandTargetingAbilitiesFromTargetingProtectedBasicLands() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent mesa = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent protectedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int mesaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mesa);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, mesaIndex, 1, List.of(protectedPlains.getId(), otherLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificingFourthLandGrantsShroudBeforeTheAbilityResolves() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent mesa = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, firstPlains, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondPlains, Keyword.SHROUD)).isFalse();

        int mesaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mesa);
        harness.activateAbilityWithMultiTargets(
                player1, mesaIndex, 1, List.of(firstPlains.getId(), secondPlains.getId()));

        harness.assertInGraveyard(player1, "Wintermoon Mesa");
        assertThat(gqs.hasKeyword(gd, firstPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondPlains, Keyword.SHROUD)).isTrue();

        harness.passBothPriorities();

        assertThat(firstPlains.isTapped()).isFalse();
        assertThat(secondPlains.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playingFourthLandRemovesShroudOnlyFromThatPlayersBasicLands() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new WintermoonMesa()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, ownPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentPlains, Keyword.SHROUD)).isTrue();

        harness.playLand(player1, 0);

        assertThat(gqs.hasKeyword(gd, ownPlains, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentPlains, Keyword.SHROUD)).isTrue();
    }
}
