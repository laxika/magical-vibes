package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReduceToMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target nonland permanent and its controller creates a 3/2 Spirit")
    void exilesTargetNonlandPermanentAndItsControllerCreatesSpirit() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castReduceToMemory(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        List<Permanent> spirits = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(spirits).singleElement().satisfies(spirit -> {
            assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spirit.getCard().getPower()).isEqualTo(3);
            assertThat(spirit.getCard().getToughness()).isEqualTo(2);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(spirit.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new ReduceToMemory()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Does not create a Spirit when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castReduceToMemory(targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"));
    }

    private void castReduceToMemory(UUID targetId) {
        harness.setHand(player1, List.of(new ReduceToMemory()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, targetId);
    }
}
