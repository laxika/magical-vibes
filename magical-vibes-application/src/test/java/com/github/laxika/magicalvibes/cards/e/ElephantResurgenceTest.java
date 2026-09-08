package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElephantResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Each player creates an Elephant whose P/T count creature cards in that player's graveyard")
    void eachPlayerCreatesElephantWithOwnGraveyardCount() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));

        castElephantResurgence();

        Permanent playerOneElephant = findElephant(player1.getId());
        Permanent playerTwoElephant = findElephant(player2.getId());
        assertThat(gqs.getEffectivePower(gd, playerOneElephant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, playerOneElephant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, playerTwoElephant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, playerTwoElephant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elephant power and toughness update as creature cards enter its controller's graveyard")
    void elephantUpdatesWithGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castElephantResurgence();

        Permanent elephant = findElephant(player1.getId());
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(2);
    }

    private void castElephantResurgence() {
        harness.setHand(player1, List.of(new ElephantResurgence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent findElephant(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Elephant".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
