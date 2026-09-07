package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DescentOfTheDragons.class, GrizzlyBears.class, Forest.class})
class DescentOfTheDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creatures and creates a Dragon for each creature's controller")
    void destroysCreaturesAndCreatesDragonsForTheirControllers() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDescent(List.of(ownBear.getId(), opponentBear.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(dragons(player1)).hasSize(1).allSatisfy(this::assertDragon);
        assertThat(dragons(player2)).hasSize(1).allSatisfy(this::assertDragon);
    }

    @Test
    @DisplayName("Creates no Dragons when no creatures are targeted")
    void resolvesWithNoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDescent(List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(dragons(player1)).isEmpty();
        assertThat(dragons(player2)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures");
    }

    private void castDescent(List<UUID> targetIds) {
        prepareCast();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DescentOfTheDragons()));
        harness.addMana(player1, ManaColor.RED, 6);
    }

    private List<Permanent> dragons(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Dragon"))
                .toList();
    }

    private void assertDragon(Permanent dragon) {
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(dragon.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
