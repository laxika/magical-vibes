package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhastlyConscription.class, FrontierMastodon.class, Plains.class, DouseInGloom.class})
class GhastlyConscriptionTest extends BaseCardTest {

    @Test
    void exilesCreatureCardsAndManifestsThemUnderTheSpellController() {
        Card creature = new FrontierMastodon();
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(creature, plains, new FrontierMastodon()));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(plains.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(2)
                .allMatch(Permanent::isFaceDown);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void doesNothingWhenTargetGraveyardHasNoCreatureCards() {
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(plains));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(plains.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent permanent = addCreatureReady(player2, new FrontierMastodon());
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    void canManifestCreaturesFromItsControllersGraveyard() {
        Card creature = new FrontierMastodon();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
                });
    }

    @Test
    void controllerCanTurnOpponentsManifestedCreatureFaceUpForItsManaCost() {
        Card creature = new FrontierMastodon();
        creature.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCard().getId()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestedOpponentsCreatureReturnsToItsOwnersGraveyardWhenItDies() {
        Card creature = new FrontierMastodon();
        creature.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GhastlyConscription(), new DouseInGloom()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, manifested.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
