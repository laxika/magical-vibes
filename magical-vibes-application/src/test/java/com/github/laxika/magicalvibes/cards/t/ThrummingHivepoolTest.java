package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrummingHivepool.class, MetallicSliver.class, GrizzlyBears.class})
class ThrummingHivepoolTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Slivers reduces the casting cost by one per Sliver")
    void affinityForSliversReducesCastingCost() {
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new ThrummingHivepool()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Slivers controlled by the spell's controller")
    void affinityDoesNotCountOtherCreaturesOrOpponentsSlivers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MetallicSliver());
        harness.setHand(player1, List.of(new ThrummingHivepool()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Grants double strike and haste to Slivers you control")
    void grantsDoubleStrikeAndHasteToOwnSliversOnly() {
        harness.addToBattlefield(player1, new ThrummingHivepool());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opponentSliver = addCreatureReady(player2, new MetallicSliver());
        Permanent nonSliver = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creates two colorless Sliver tokens during your upkeep")
    void createsTwoSliverTokensDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new ThrummingHivepool());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Sliver");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("Does not create tokens during an opponent's upkeep")
    void doesNotCreateTokensDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ThrummingHivepool());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }
}
