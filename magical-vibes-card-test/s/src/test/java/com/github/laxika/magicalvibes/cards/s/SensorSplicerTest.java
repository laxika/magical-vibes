package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MaulSplicer;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SensorSplicer.class, MaulSplicer.class, Xenograft.class})
class SensorSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Sensor Splicer + Golem token

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");
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
        resolveAllTriggers();

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Sensor Splicer itself does not have vigilance (not a Golem)")
    void sensorSplicerDoesNotHaveVigilance() {
        harness.addToBattlefield(player1, new SensorSplicer());

        Permanent sensorSplicer = findPermanent(player1, "Sensor Splicer");

        assertThat(gqs.hasKeyword(gd, sensorSplicer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Golems do not get vigilance")
    void opponentGolemsDoNotGetVigilance() {
        harness.addToBattlefield(player1, new SensorSplicer());

        harness.setHand(player2, List.of(new MaulSplicer()));
        harness.addMana(player2, ManaColor.GREEN, 7);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(2);
        for (Permanent golem : findPermanents(player2, "Phyrexian Golem")) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isFalse();
        }
    }

    @Test
    @DisplayName("Vigilance is lost when Sensor Splicer leaves the battlefield")
    void vigilanceLostWhenSensorSplicerLeaves() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isTrue();

        // Remove Sensor Splicer from battlefield
        Permanent sensorSplicer = findPermanent(player1, "Sensor Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(sensorSplicer);

        // Golem should no longer have vigilance
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Created token is colorless and enters untapped")
    void tokenIsColorlessAndUntapped() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(golem.getCard().isToken()).isTrue();
        assertThat(golem.getCard().getColor()).isNull();
        assertThat(golem.getCard().getColors()).isEmpty();
        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Golem attacks without tapping while Sensor Splicer remains")
    void golemAttacksWithoutTapping() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        golem.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(golem)));

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB still creates a Golem after Sensor Splicer leaves")
    void tokenTriggerResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new SensorSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent splicer = findPermanent(player1, "Sensor Splicer");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, splicer);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Sensor Splicer gains its own vigilance when Xenograft makes it a Golem")
    void splicerHasVigilanceWhenItBecomesGolem() {
        harness.addToBattlefield(player1, new SensorSplicer());
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOLEM");
        resolveAllTriggers();

        Permanent splicer = findPermanent(player1, "Sensor Splicer");
        assertThat(gqs.hasEffectiveSubtype(gd, splicer, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.hasKeyword(gd, splicer, Keyword.VIGILANCE)).isTrue();
    }
}
