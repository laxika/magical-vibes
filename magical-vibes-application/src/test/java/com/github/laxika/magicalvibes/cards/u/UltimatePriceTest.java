package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void prepare() {
        harness.setHand(player1, List.of(new UltimatePrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castPrice(Permanent target) {
        prepare();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
