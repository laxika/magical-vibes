package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimatePrice.class, AxebaneGuardian.class, EtherealArmor.class})
class UltimatePriceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a monocolored creature")
    void destroysMonocoloredCreature() {
        Permanent mono = addCreature(player2, "Mono Creature", CardColor.GREEN, null);

        castPrice(mono);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(mono.getId()));
        harness.assertInGraveyard(player2, "Mono Creature");
    }

    @Test
    @DisplayName("Cannot target a multicolored creature")
    void cannotTargetMulticoloredCreature() {
        Permanent gold = addCreature(player2, "Gold Creature", CardColor.GREEN, CardColor.WHITE);

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gold.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a colorless creature")
    void cannotTargetColorlessCreature() {
        Permanent colorless = addCreature(player2, "Colorless Creature", null, null);

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, colorless.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves the creature — destruction is not regeneration-proof")
    void regenerationShieldSavesTheCreature() {
        Permanent mono = addCreature(player2, "Mono Creature", CardColor.BLACK, null);
        mono.setRegenerationShield(1);

        castPrice(mono);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(mono.getId()));
    }

    @Test
    @DisplayName("Can destroy its controller's monocolored creature")
    void destroysOwnCreature() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());

        castPrice(guardian);

        harness.assertNotOnBattlefield(player1, "Axebane Guardian");
        harness.assertInGraveyard(player1, "Axebane Guardian");
    }

    @Test
    @DisplayName("Cannot target a monocolored noncreature permanent")
    void cannotTargetMonocoloredEnchantment() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new AxebaneGuardian());
        Permanent armor = harness.addToBattlefieldAndReturn(player2, new EtherealArmor());
        armor.setAttachedTo(guardian.getId());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, armor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a target that becomes multicolored before resolution")
    void targetBecomesMulticolored() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new AxebaneGuardian());
        prepare();
        harness.castInstant(player1, 0, guardian.getId());

        guardian.getGrantedColors().add(CardColor.BLUE);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Axebane Guardian");
        harness.assertInGraveyard(player1, "Ultimate Price");
        assertThat(gd.stack).isEmpty();
    }

    private void prepare() {
        harness.setHand(player1, List.of(new UltimatePrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castPrice(Permanent target) {
        prepare();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    /**
     * Adds a 2/2 creature. A single non-null {@code primary} color makes it monocolored; passing both
     * colors makes it multicolored; passing {@code null, null} makes it colorless.
     */
    private Permanent addCreature(Player player, String name, CardColor primary, CardColor secondary) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        if (primary != null && secondary != null) {
            card.setColors(List.of(primary, secondary));
        } else if (primary != null) {
            card.setColor(primary);
        }
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
