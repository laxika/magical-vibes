package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiresKiss.class, PersistentSpecimen.class})
class VampiresKissTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses 2 life, controller gains 2 life, and two Blood tokens are created")
    void drainsOpponentAndCreatesTwoBloodTokens() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Blood")).hasSize(2)
                .allSatisfy(blood -> {
                    assertThat(blood.getCard().isToken()).isTrue();
                    assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
                    assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
                });
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Self-targeting at 1 life completes life gain before checking for a loss")
    void survivesSelfTargetingAtOneLife() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    @DisplayName("Created Blood can immediately discard and sacrifice to draw, leaving the other token")
    void bloodTokenCanBeUsedImmediately() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        Permanent blood = findPermanents(player1, "Blood").getFirst();
        PersistentSpecimen discarded = new PersistentSpecimen();
        VampiresKiss drawn = new VampiresKiss();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Blood cannot be activated without a card to discard")
    void bloodRequiresDiscardCard() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Blood")).hasSize(2)
                .allSatisfy(blood -> assertThat(blood.isTapped()).isFalse());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
