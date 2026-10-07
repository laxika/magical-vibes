package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TunnelSurveyor.class, Murder.class})
class TunnelSurveyorTest extends BaseCardTest {

    @Test
    void entersAndCreatesGlimmerEnchantmentCreatureToken() {
        harness.setHand(player1, List.of(new TunnelSurveyor()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
        assertThat(glimmer.getCard().isToken()).isTrue();
        assertThat(glimmer.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(1);
        assertThat(glimmer.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Glimmer")).isEqualTo(1);
        assertThat(countPermanents(player2, "Glimmer")).isZero();
    }

    @Test
    void tokenCreationWaitsForTriggerAndSurvivesSourceDestruction() {
        harness.setHand(player1, List.of(new TunnelSurveyor()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tunnel Surveyor");
        harness.assertNotOnBattlefield(player1, "Glimmer");

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Tunnel Surveyor"));

        harness.assertInGraveyard(player1, "Tunnel Surveyor");
        harness.assertNotOnBattlefield(player1, "Tunnel Surveyor");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Glimmer")).isEqualTo(1);
        assertThat(countPermanents(player2, "Glimmer")).isZero();
    }
}
