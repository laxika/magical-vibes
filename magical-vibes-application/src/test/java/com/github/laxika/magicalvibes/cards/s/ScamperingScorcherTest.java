package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({ScamperingScorcher.class, AirElemental.class, GrizzlyBears.class, Dominate.class, Unsummon.class})
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
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(existingElemental.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELEMENTAL)))
                .allSatisfy(elemental -> assertThat(elemental.hasKeyword(Keyword.HASTE)).isFalse());
    }

    @Test
    @DisplayName("Elementals entering after the trigger resolves do not gain haste")
    void laterElementalsDoNotGainHaste() {
        castScamperingScorcher();

        Permanent laterElemental = harness.enterBattlefieldAndReturn(player1, new AirElemental());
        resolveAllTriggers();

        assertThat(laterElemental.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed({Unsummon.class})
    @DisplayName("The trigger creates hasty tokens even if its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent scorcher = harness.enterBattlefieldAndReturn(player1, new ScamperingScorcher());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, scorcher.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Scampering Scorcher");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @CardUsed({Dominate.class})
    @DisplayName("The source does not gain haste if an opponent takes it before resolution")
    void stolenSourceDoesNotGainHaste() {
        Permanent existingElemental = addCreatureReady(player1, new AirElemental());
        Permanent scorcher = harness.enterBattlefieldAndReturn(player1, new ScamperingScorcher());
        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, 4, scorcher.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scorcher);
        resolveAllTriggers();

        assertThat(scorcher.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(existingElemental.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2)
                .allSatisfy(token -> assertThat(token.hasKeyword(Keyword.HASTE)).isTrue());
    }

    private void castScamperingScorcher() {
        harness.setHand(player1, List.of(new ScamperingScorcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
