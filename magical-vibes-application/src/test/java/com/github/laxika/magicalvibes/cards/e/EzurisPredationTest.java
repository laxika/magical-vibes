package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisPredation.class, HillGiant.class, AirElemental.class})
class EzurisPredationTest extends BaseCardTest {

    @Test
    void createsOneBeastForEachOpposingCreatureAndEachFightsADifferentCreature() {
        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new AirElemental());

        castEzurisPredation();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Air Elemental");

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void doesNotCreateTokensWhenOpponentsControlNoCreatures() {
        castEzurisPredation();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void castEzurisPredation() {
        harness.setHand(player1, List.of(new EzurisPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
