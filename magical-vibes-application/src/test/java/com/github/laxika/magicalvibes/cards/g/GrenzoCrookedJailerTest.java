package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RooftopStorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrenzoCrookedJailer.class, Forest.class, GrizzlyBears.class, Opt.class, RooftopStorm.class})
class GrenzoCrookedJailerTest extends BaseCardTest {

    @Test
    void entersWithAHeistTrigger() {
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, first, second, third));

        harness.addToBattlefield(player1, new GrenzoCrookedJailer());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.HeistCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HeistCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3).allMatch(card -> !card.hasType(CardType.LAND));

        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
    }

    @Test
    void castsAnOpponentOwnedSmallSpellForFreeOnceEachTurn() {
        harness.addToBattlefield(player1, new GrenzoCrookedJailer());
        Opt first = new Opt();
        first.setOwnerId(player2.getId());
        Opt second = new Opt();
        second.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(first, second));

        harness.castInstant(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotMakeOwnedOrLargeSpellsFree() {
        harness.addToBattlefield(player1, new GrenzoCrookedJailer());
        Opt owned = new Opt();
        owned.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(owned));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        RooftopStorm large = new RooftopStorm();
        large.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(large));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
