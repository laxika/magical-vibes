package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostFairLureFish.class, DawnhartDisciple.class, GrizzlyBears.class})
class FrostFairLureFishTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates Fish and tapped Treasures, and Fish gain haste")
    void entersWithFishAndTappedTreasures() {
        FrostFairLureFish card = new FrostFairLureFish();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> fish = findPermanents(player1, "Fish");
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(fish).hasSize(2);
        assertThat(fish).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FISH);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(treasures).hasSize(2);
        assertThat(treasures).allSatisfy(token -> assertThat(token.isTapped()).isTrue());
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Frost Fair Lure Fish"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fish you control cannot be blocked by Humans")
    void fishCannotBeBlockedByHumans() {
        Permanent fish = addCreatureReady(player1, new FrostFairLureFish());
        fish.setAttacking(true);
        Permanent human = addCreatureReady(player2, new DawnhartDisciple());
        Permanent nonHuman = addCreatureReady(player2, new GrizzlyBears());
        var blockContext = bls.createBlockLegalityContext(gd, gd.playerBattlefields.get(player2.getId()));

        assertThat(bls.canBlockAttacker(blockContext, human, fish)).isFalse();
        assertThat(bls.canBlockAttacker(blockContext, nonHuman, fish)).isTrue();
    }

    @Test
    @DisplayName("Frost Fair Lure Fish can be foretold")
    void canBeForetold() {
        FrostFairLureFish card = new FrostFairLureFish();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(card.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
    }
}
