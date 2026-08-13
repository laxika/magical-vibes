package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhenaxGodOfDeceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Phenax is not a creature below seven combined blue and black devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent phenax = addPhenax();
        addBlackDevotion(4);

        assertThat(gqs.isCreature(gd, phenax)).isFalse();
        assertThat(gqs.isEnchantment(gd, phenax)).isTrue();
    }

    @Test
    @DisplayName("Phenax becomes a creature at seven combined blue and black devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent phenax = addPhenax();
        addBlackDevotion(5);

        assertThat(gqs.isCreature(gd, phenax)).isTrue();
    }

    @Test
    @DisplayName("A creature you control can tap to mill cards equal to its toughness")
    void creatureMillsEqualToItsToughness() {
        Permanent phenax = addPhenax();
        addBlackDevotion(5);
        phenax.setSummoningSick(false);
        int deckSizeBefore = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(phenax.isTapped()).isTrue();
    }

    private Permanent addPhenax() {
        return harness.addToBattlefieldAndReturn(player1, new PhenaxGodOfDeception());
    }

    private void addBlackDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }

    private int trimDeck(com.github.laxika.magicalvibes.model.Player player, int size) {
        var deck = gd.playerDecks.get(player.getId());
        while (deck.size() > size) {
            deck.removeFirst();
        }
        return deck.size();
    }
}
