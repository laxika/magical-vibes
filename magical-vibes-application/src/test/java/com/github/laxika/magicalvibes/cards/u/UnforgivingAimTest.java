package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnforgivingAimTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 destroys a creature with flying")
    void destroysCreatureWithFlying() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Mode 0 rejects a creature without flying")
    void rejectsCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Mode 1 destroys an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Mode 2 creates a 2/2 black and green Elf token")
    void createsElfToken() {
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        Permanent elf = findPermanent(player1, "Elf");
        assertThat(elf.getCard().isToken()).isTrue();
        assertThat(elf.getCard().getPower()).isEqualTo(2);
        assertThat(elf.getCard().getToughness()).isEqualTo(2);
        assertThat(elf.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(elf.getCard().getSubtypes()).containsExactly(CardSubtype.ELF);
    }
}
