package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZendikarsRoil.class, Forest.class})
class ZendikarsRoilTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall — playing a land creates a 2/2 Elemental token")
    void landfallCreatesElemental() {
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(elemental.getCard().isToken()).isTrue();
        assertThat(elemental.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elemental.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent playing a land does not create an Elemental")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Lands entering without being played each trigger landfall")
    void landsEnteringWithoutBeingPlayedTriggerSeparately() {
        harness.addToBattlefield(player1, new ZendikarsRoil());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Each Roil triggers independently for the same land")
    void multipleCopiesEachCreateAToken() {
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
    }

    @Test
    @DisplayName("Roil entering does not trigger from lands already present")
    void existingLandsDoNotTriggerWhenRoilEnters() {
        harness.addToBattlefield(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new ZendikarsRoil());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }
}
