package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class GallantStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with toughness 4 or greater")
    void destroysCreatureWithEnoughToughness() {
        Permanent creature = addCreature(player2, "Large Creature", 4);

        castGallantStrike(creature);

        harness.assertNotOnBattlefield(player2, "Large Creature");
        harness.assertInGraveyard(player2, "Large Creature");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness less than 4")
    void cannotTargetCreatureWithLowToughness() {
        Permanent creature = addCreature(player2, "Small Creature", 3);

        assertThatThrownBy(() -> castGallantStrike(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Gallant Strike and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gallant Strike");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castGallantStrike(Permanent target) {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, String name, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
