package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.cards.s.StirringWildwood;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Terastodon.class, TectonicEdge.class, LeatherbackBaloth.class, StirringWildwood.class})
class TerastodonTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to three noncreature permanents and gives each controller an Elephant per permanent destroyed")
    void destroysPermanentsAndCreatesTokensForTheirControllers() {
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new TectonicEdge());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        Permanent opponentPlains2 = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());

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
        Permanent indestructiblePlains = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        indestructiblePlains.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castTerastodon(List.of(indestructiblePlains.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(indestructiblePlains.getId()));
        assertThat(elephants(player2)).isEmpty();
    }

    @Test
    @DisplayName("Can resolve with no chosen targets")
    void resolvesWithNoTargets() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());

        castTerastodon(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(plains.getId()));
        assertThat(elephants(player1)).isEmpty();
        assertThat(elephants(player2)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new LeatherbackBaloth());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May decline destruction at resolution after choosing targets")
    void mayDeclineDestructionAfterChoosingTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(elephants(player1)).isEmpty();
        assertThat(elephants(player2)).isEmpty();
    }

    @Test
    @DisplayName("Only destroyed targets create tokens when another target is indestructible")
    void mixedDestructionCreatesOnlyOneToken() {
        Permanent surviving = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        surviving.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());

        castTerastodon(List.of(surviving.getId(), destroyed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(surviving).doesNotContain(destroyed);
        assertThat(elephants(player2)).hasSize(1).allSatisfy(this::assertElephant);
    }

    @Test
    @DisplayName("Resolves for remaining targets when one target leaves the battlefield")
    void removedTargetDoesNotCreateToken() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        prepareCast();
        harness.castCreature(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();
        acceptDestructionIfPrompted();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(remaining);
        assertThat(elephants(player2)).hasSize(1).allSatisfy(this::assertElephant);
    }

    @Test
    @DisplayName("A targeted land that becomes a creature is left untouched")
    void animatedTargetIsNotDestroyed() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        Permanent wildwood = harness.addToBattlefieldAndReturn(player2, new StirringWildwood());
        wildwood.untap();
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId(), wildwood.getId()));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        acceptDestructionIfPrompted();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wildwood).doesNotContain(land);
        assertThat(elephants(player2)).hasSize(1).allSatisfy(this::assertElephant);
    }

    private void acceptDestructionIfPrompted() {
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    private List<Permanent> elephants(Player player) {
        return findPermanents(player, "Elephant").stream()
                .filter(permanent -> permanent.getCard().isToken())
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
        acceptDestructionIfPrompted();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Terastodon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
