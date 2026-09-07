package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed(RadiantPurge.class)
class RadiantPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a multicolored creature")
    void exilesMulticoloredCreature() {
        Permanent target = addPermanent(player2, "Multicolored Creature", CardType.CREATURE,
                CardColor.GREEN, CardColor.WHITE);

        castRadiantPurge(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Multicolored Creature");
        harness.assertNotInGraveyard(player2, "Multicolored Creature");
    }

    @Test
    @DisplayName("Exiles a multicolored enchantment")
    void exilesMulticoloredEnchantment() {
        Permanent target = addPermanent(player2, "Multicolored Enchantment", CardType.ENCHANTMENT,
                CardColor.GREEN, CardColor.WHITE);

        castRadiantPurge(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Multicolored Enchantment");
    }

    @Test
    @DisplayName("Cannot target a monocolored creature")
    void cannotTargetMonocoloredCreature() {
        Permanent target = addPermanent(player2, "Monocolored Creature", CardType.CREATURE, CardColor.GREEN);

        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a multicolored artifact")
    void cannotTargetMulticoloredArtifact() {
        Permanent target = addPermanent(player2, "Multicolored Artifact", CardType.ARTIFACT,
                CardColor.GREEN, CardColor.WHITE);

        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRadiantPurge(Permanent target) {
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new RadiantPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addPermanent(Player player, String name, CardType type, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setColors(List.of(colors));
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
