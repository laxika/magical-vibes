package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerastodonTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to three noncreature permanents and gives each controller an Elephant per permanent destroyed")
    void destroysPermanentsAndCreatesTokensForTheirControllers() {
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent opponentPlains2 = harness.addToBattlefieldAndReturn(player2, new Plains());

        castTerastodon(List.of(ownPlains.getId(), opponentPlains.getId(), opponentPlains2.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownPlains.getId()))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentPlains.getId())
                        || permanent.getId().equals(opponentPlains2.getId()))
                .hasSize(2);

        assertThat(elephants(player1)).hasSize(1);
        assertThat(elephants(player2)).hasSize(2);
        assertThat(elephants(player1)).allSatisfy(this::assertElephant);
        assertThat(elephants(player2)).allSatisfy(this::assertElephant);
    }

    @Test
    @DisplayName("Does not create a token when destruction is prevented")
    void indestructiblePermanentDoesNotCreateToken() {
        Permanent indestructiblePlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        indestructiblePlains.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castTerastodon(List.of(indestructiblePlains.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(indestructiblePlains.getId()));
        assertThat(elephants(player2)).isEmpty();
    }

    @Test
    @DisplayName("Can resolve with no chosen targets")
    void resolvesWithNoTargets() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        castTerastodon(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(plains.getId()));
        assertThat(elephants(player1)).isEmpty();
        assertThat(elephants(player2)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> elephants(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elephant"))
                .toList();
    }

    private void assertElephant(Permanent elephant) {
        assertThat(elephant.getCard().getPower()).isEqualTo(3);
        assertThat(elephant.getCard().getToughness()).isEqualTo(3);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).containsExactly(CardSubtype.ELEPHANT);
        assertThat(elephant.getCard().isToken()).isTrue();
    }

    private void castTerastodon(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Terastodon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
