package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReidaneGodOfTheWorthy.class, SnowCoveredPlains.class, DayOfJudgment.class,
        GrizzlyBears.class, Shock.class, ZuranSpellcaster.class})
class ReidaneGodOfTheWorthyTest extends BaseCardTest {

    @Test
    void opponentsSnowLandsEnterTapped() {
        harness.addToBattlefield(player1, new ReidaneGodOfTheWorthy());
        harness.setHand(player2, List.of(new SnowCoveredPlains()));
        prepareOpponentMainPhase();

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Snow-Covered Plains").isTapped()).isTrue();
    }

    @Test
    void controllersSnowLandsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ReidaneGodOfTheWorthy());
        harness.setHand(player1, List.of(new SnowCoveredPlains()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Snow-Covered Plains").isTapped()).isFalse();
    }

    @Test
    void opponentsHighManaValueNoncreatureSpellsCostTwoMore() {
        harness.addToBattlefield(player1, new ReidaneGodOfTheWorthy());
        harness.setHand(player2, List.of(new DayOfJudgment()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        prepareOpponentMainPhase();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void castingBackFaceUsesItsTargetingAbility() {
        harness.setHand(player1, List.of(new ReidaneGodOfTheWorthy()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void backFaceCountersOpponentSpellTargetingControlledPermanent() {
        addValkmira(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void backFaceCountersOpponentAbilityTargetingController() {
        addValkmira(player1);
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void backFacePreventsOneDamageFromOpponentSourceWhenOpponentPays() {
        addValkmira(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addValkmira(Player player) {
        ReidaneGodOfTheWorthy card = new ReidaneGodOfTheWorthy();
        return harness.addToBattlefieldAndReturn(player, card.getBackFaceCard());
    }
}
