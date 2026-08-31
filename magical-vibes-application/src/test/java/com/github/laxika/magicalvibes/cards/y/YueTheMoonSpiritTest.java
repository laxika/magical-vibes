package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YueTheMoonSpirit.class, GrizzlyBears.class, Divination.class})
class YueTheMoonSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps five other creatures and offers a noncreature spell for free")
    void waterbendCastsNoncreatureSpellForFree() {
        Permanent yue = addCreatureReady(player1, new YueTheMoonSpirit());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        Permanent fourth = addCreatureReady(player1, new GrizzlyBears());
        Permanent fifth = addCreatureReady(player1, new GrizzlyBears());
        Divination spell = new Divination();
        harness.setHand(player1, List.of(spell));

        harness.activateAbility(player1, 0, null, null);

        assertThat(yue.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(fourth.isTapped()).isTrue();
        assertThat(fifth.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Does not offer a creature spell")
    void doesNotOfferCreatureSpell() {
        addCreatureReady(player1, new YueTheMoonSpirit());
        addWaterbendPermanents();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without five other untapped creatures or artifacts")
    void cannotActivateWithoutFiveWaterbendPermanents() {
        Permanent yue = addCreatureReady(player1, new YueTheMoonSpirit());
        addWaterbendPermanents(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(yue.isTapped()).isFalse();
    }

    private void addWaterbendPermanents() {
        addWaterbendPermanents(5);
    }

    private void addWaterbendPermanents(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }
}
