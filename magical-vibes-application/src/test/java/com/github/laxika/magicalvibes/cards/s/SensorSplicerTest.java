package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensorSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Sensor Splicer has ETB token effect and static vigilance grant")
    void hasCorrectEffects() {
        SensorSplicer card = new SensorSplicer();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Phyrexian Golem");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Sensor Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golemToken.getEffectivePower()).isEqualTo(3);
        assertThat(golemToken.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golem token has vigilance from Sensor Splicer's static ability")
    void golemTokenHasVigilance() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Sensor Splicer itself does not have vigilance (not a Golem)")
    void sensorSplicerDoesNotHaveVigilance() {
        harness.addToBattlefield(player1, new SensorSplicer());

        Permanent sensorSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sensor Splicer"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, sensorSplicer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Golems do not get vigilance")
    void opponentGolemsDoNotGetVigilance() {
        harness.addToBattlefield(player1, new SensorSplicer());

        harness.setHand(player2, List.of(new SensorSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent p2SensorSplicer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sensor Splicer"))
                .findFirst()
                .orElseThrow();

        // Player 2's Sensor Splicer should not get vigilance from Player 1's Sensor Splicer
        assertThat(gqs.hasKeyword(gd, p2SensorSplicer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is lost when Sensor Splicer leaves the battlefield")
    void vigilanceLostWhenSensorSplicerLeaves() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isTrue();

        // Remove Sensor Splicer from battlefield
        Permanent sensorSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sensor Splicer"))
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(sensorSplicer);

        // Golem should no longer have vigilance
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isFalse();
    }
}
