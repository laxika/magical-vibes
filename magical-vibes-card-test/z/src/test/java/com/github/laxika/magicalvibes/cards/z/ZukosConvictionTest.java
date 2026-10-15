package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZukosConviction.class, GrizzlyBears.class, HolyDay.class})
class ZukosConvictionTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToHandWithoutKicker() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetCreatureToBattlefieldTappedWhenKicked() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellCannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotTargetCreatureInOpponentsGraveyard(boolean kicked) {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> {
            if (kicked) {
                harness.castKickedInstant(player1, 0, creature.getId());
            } else {
                harness.castInstant(player1, 0, creature.getId());
            }
        }).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard(boolean kicked) {
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(other.getId())
                .doesNotContain(target.getId());
        harness.assertInGraveyard(player1, "Zuko's Conviction");
    }

    @Test
    void cannotKickWithoutPayingFullAdditionalCost() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ZukosConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
