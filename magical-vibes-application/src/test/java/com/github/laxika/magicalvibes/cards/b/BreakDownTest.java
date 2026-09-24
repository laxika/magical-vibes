package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakDown.class, Forest.class, GildedLotus.class, GloriousAnthem.class, GrizzlyBears.class})
class BreakDownTest extends BaseCardTest {

    @Test
    void destroysArtifactAndCreatesJunk() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());

        cast(artifact.getId());

        harness.assertNotOnBattlefield(player2, "Gilded Lotus");
        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getSubtypes()).containsExactly(CardSubtype.JUNK);
    }

    @Test
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void junkExilesTopCardWhenSacrificed() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());

        Permanent junk = findPermanent(player1, "Junk");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(junk.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
    }

    private void cast(java.util.UUID targetId) {
        prepareSpell();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BreakDown()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
