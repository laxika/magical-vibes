package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindswiftSlice.class, GrizzlyBears.class, HillGiant.class})
class WindswiftSliceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Elf Warrior for one excess damage")
    void createsTokensForExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(source, target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> tokens = findPermanents(player1, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Creates no tokens when no excess damage is dealt")
    void createsNoTokensWithoutExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(source, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Requires a controlled creature and an opponent's creature")
    void enforcesTargetRestrictions() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WindswiftSlice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent source, Permanent target) {
        harness.setHand(player1, List.of(new WindswiftSlice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
