package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({UnforgivingAim.class, SerraAngel.class, GrizzlyBears.class, GloriousAnthem.class})
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

    @Test
    @DisplayName("Mode 1 rejects a nonenchantment creature")
    void rejectsNonenchantmentCreature() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, 1, harness.getPermanentId(player2, "Serra Angel")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    @Test
    @DisplayName("Can destroy an enchantment controlled by the caster")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player1, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("A removed target does not cause the spell to use another mode")
    void removedTargetDoesNotCreateTokenOrDestroyAnotherPermanent() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new UnforgivingAim(), new UnforgivingAim()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        var targetId = harness.getPermanentId(player2, "Serra Angel");

        harness.castInstant(player1, 0, 0, targetId);
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Elf");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Unforgiving Aim"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
