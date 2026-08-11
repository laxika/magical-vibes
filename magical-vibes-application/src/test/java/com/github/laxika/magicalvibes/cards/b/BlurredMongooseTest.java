package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlurredMongooseTest extends BaseCardTest {

    @Test
    @DisplayName("This spell can't be countered")
    void cannotBeCountered() {
        BlurredMongoose mongoose = new BlurredMongoose();
        harness.setHand(player1, List.of(mongoose));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, mongoose.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blurred Mongoose");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud prevents the creature from being targeted")
    void shroudPreventsTargeting() {
        harness.setHand(player1, List.of(new BlurredMongoose()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mongoose = findPermanent(player1, "Blurred Mongoose");
        assertThat(gqs.hasKeyword(gd, mongoose, Keyword.SHROUD)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
