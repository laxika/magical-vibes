package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheBrokenSky;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, InvasionOfTolvada.class, Shock.class, TheBrokenSky.class})
class InvasionOfTolvadaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted nonbattle permanent card from the graveyard")
    void etbReturnsNonbattlePermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        InvasionOfTolvada battle = new InvasionOfTolvada();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, battle, shock));

        castInvasion();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Invasion of Tolvada");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Defeating the Siege casts The Broken Sky transformed")
    void defeatCastsBackFace() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTolvada());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brokenSky = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TheBrokenSky)
                .findFirst()
                .orElseThrow();
        assertThat(brokenSky.isTransformed()).isTrue();
        assertThat(brokenSky.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }

    @Test
    @DisplayName("The Broken Sky boosts token creatures and creates a lifelink Spirit at your end step")
    void backFaceBoostsTokensAndCreatesSpirit() {
        Permanent brokenSky = harness.addToBattlefieldAndReturn(player1, new InvasionOfTolvada());
        brokenSky.setCard(brokenSky.getOriginalCard().getBackFaceCard());
        brokenSky.setTransformed(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Spirit".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isTrue();
    }

    private void castInvasion() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InvasionOfTolvada()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
    }
}
