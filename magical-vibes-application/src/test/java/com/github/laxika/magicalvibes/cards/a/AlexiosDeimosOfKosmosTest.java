package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AlexiosDeimosOfKosmos.class, CruelEdict.class})
class AlexiosDeimosOfKosmosTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep gives them control, untaps Alexios, adds a counter, and grants haste")
    void eachPlayersUpkeepTransfersAlexiosAndImprovesIt() {
        Permanent alexios = addCreatureReady(player1, new AlexiosDeimosOfKosmos());
        alexios.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(alexios);
        assertThat(alexios.isTapped()).isFalse();
        assertThat(alexios.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, alexios, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Alexios must attack but cannot attack its owner")
    void attackRestrictionsApply() {
        AlexiosDeimosOfKosmos card = new AlexiosDeimosOfKosmos();
        card.setOwnerId(player2.getId());
        Permanent alexios = addCreatureReady(player2, card);

        assertThat(als.canAttackDefender(gd, alexios, player2.getId())).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Alexios cannot be sacrificed")
    void cannotBeSacrificed() {
        Permanent alexios = addCreatureReady(player2, new AlexiosDeimosOfKosmos());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(alexios);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(alexios.getCard());
    }
}
