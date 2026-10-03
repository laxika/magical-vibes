package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlexiosDeimosOfKosmos.class, CruelEdict.class, Humble.class, JaceBeleren.class})
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

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void ownersUpkeepReturnsControlAndAddsAnotherCounter() {
        Permanent alexios = addCreatureReady(player1, new AlexiosDeimosOfKosmos());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        alexios.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alexios);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(alexios);
        assertThat(alexios.isTapped()).isFalse();
        assertThat(alexios.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, alexios, Keyword.HASTE)).isTrue();
    }

    @Test
    void canAttackPlaneswalkerControlledByOwner() {
        AlexiosDeimosOfKosmos card = new AlexiosDeimosOfKosmos();
        card.setOwnerId(player1.getId());
        Permanent alexios = addCreatureReady(player1, card);
        Permanent jace = harness.enterBattlefieldAndReturn(player1, new JaceBeleren());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, alexios, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, alexios, jace.getId())).isTrue();
    }

    @Test
    void losingAbilitiesAllowsAttackingOwner() {
        AlexiosDeimosOfKosmos card = new AlexiosDeimosOfKosmos();
        card.setOwnerId(player1.getId());
        Permanent alexios = addCreatureReady(player2, card);

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, alexios.getId());
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, alexios, player1.getId())).isTrue();
    }

    @Test
    void losingAbilitiesAllowsBeingSacrificed() {
        AlexiosDeimosOfKosmos card = new AlexiosDeimosOfKosmos();
        card.setOwnerId(player1.getId());
        Permanent alexios = addCreatureReady(player2, card);

        harness.setHand(player1, List.of(new Humble(), new CruelEdict()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, alexios.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(alexios);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
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
