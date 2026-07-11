package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VioletPallTest extends BaseCardTest {

    // "Destroy target nonblack creature. Create a 1/1 black Faerie Rogue creature token with flying."

    @Test
    @DisplayName("Destroys a nonblack creature and creates a flying Faerie Rogue token")
    void destroysNonblackCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Faerie Rogue".equals(p.getCard().getName()))
                .filter(p -> p.getCard().getKeywords().contains(Keyword.FLYING))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 5);

        UUID blackId = harness.getPermanentId(player2, "Bog Wraith");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(blackId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> "Bog Wraith".equals(p.getCard().getName()))).isTrue();
    }
}
