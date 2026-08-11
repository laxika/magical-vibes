package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScamperingScorcherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two Elementals and gives your Elementals haste")
    void etbCreatesElementalsAndGrantsHaste() {
        Permanent existingElemental = addCreatureReady(player1, new AirElemental());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElemental = addCreatureReady(player2, new AirElemental());

        castScamperingScorcher();

        List<Permanent> ownElementals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELEMENTAL))
                .toList();
        assertThat(ownElementals).hasSize(4);
        assertThat(ownElementals).allSatisfy(elemental -> {
            assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
            assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
        assertThat(ownElementals.stream()
                .filter(elemental -> elemental.getCard().isToken()
                        && elemental.getCard().getPower() == 1
                        && elemental.getCard().getToughness() == 1))
                .hasSize(2);
        assertThat(ownBear.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(opponentElemental.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(existingElemental.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Elemental haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent existingElemental = addCreatureReady(player1, new AirElemental());
        castScamperingScorcher();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(existingElemental.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELEMENTAL)))
                .allSatisfy(elemental -> assertThat(elemental.hasKeyword(Keyword.HASTE)).isFalse());
    }

    private void castScamperingScorcher() {
        harness.setHand(player1, List.of(new ScamperingScorcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
