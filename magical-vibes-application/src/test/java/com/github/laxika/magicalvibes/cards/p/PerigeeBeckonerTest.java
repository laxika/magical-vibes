package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerigeeBeckoner.class, GrizzlyBears.class, DoomBlade.class})
class PerigeeBeckonerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control +2/+0 and a death return ability")
    void etbBoostsAnotherCreatureAndGrantsDeathReturn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBeckonerTargeting(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        Card bearsCard = bears.getCard();
        destroy(player2, bears);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bearsCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ETB ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PerigeeBeckoner()));
        addBeckonerMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("The ETB ability cannot target Perigee Beckoner itself")
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new PerigeeBeckoner()));
        addBeckonerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The granted death return ability expires at end of turn")
    void deathReturnExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card bearsCard = bears.getCard();
        castBeckonerTargeting(bears);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        destroy(player2, bears);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bearsCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bearsCard.getId()));
    }

    @Test
    @DisplayName("A returned creature loses the boost and cannot return a second time")
    void returnedCreatureDoesNotRetainTemporaryEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card bearsCard = bears.getCard();
        castBeckonerTargeting(bears);

        destroy(player2, bears);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bearsCard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);

        destroy(player2, returned);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bearsCard);
    }

    @Test
    @DisplayName("Warp exiles Beckoner at the next end step and permits a later normal cast")
    void warpExilesAndAllowsCastingOnALaterTurn() {
        PerigeeBeckoner beckoner = new PerigeeBeckoner();
        harness.setHand(player1, List.of(beckoner));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Perigee Beckoner");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Perigee Beckoner");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(beckoner.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, beckoner.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        addBeckonerMana();
        harness.castFromExile(player1, beckoner.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Perigee Beckoner");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(beckoner.getId()));

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Perigee Beckoner");
    }

    private void castBeckonerTargeting(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PerigeeBeckoner()));
        addBeckonerMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addBeckonerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
