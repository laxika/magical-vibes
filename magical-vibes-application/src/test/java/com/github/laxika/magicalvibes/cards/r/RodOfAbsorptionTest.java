package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RodOfAbsorption.class, Divination.class, Opt.class})
class RodOfAbsorptionTest extends BaseCardTest {

    @Test
    void exilesResolvingInstantOrSorceryWithRodTracking() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(rod.getId()))
                .extracting(Card::getId)
                .containsExactly(divination.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(divination.getId());
    }

    @Test
    void sacrificesAndCastsAnyNumberWithinTheXManaValueLimit() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination divination = new Divination();
        gd.addToExile(player1.getId(), opt, rod.getId());
        gd.addToExile(player1.getId(), divination, rod.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(opt.getId(), divination.getId());
        assertThat(choice.maxTotalManaValue()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(opt.getId(), divination.getId()));

        assertThat(gd.findExiledCard(opt.getId())).isNull();
        assertThat(gd.findExiledCard(divination.getId())).isNull();
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .contains(opt.getId(), divination.getId());
    }

    @Test
    void rejectsASelectionExceedingX() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination divination = new Divination();
        gd.addToExile(player1.getId(), opt, rod.getId());
        gd.addToExile(player1.getId(), divination, rod.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(opt.getId(), divination.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }
}
