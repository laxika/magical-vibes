package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({RebuildTheCity.class, Mountain.class, GrizzlyBears.class})
class RebuildTheCityTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 3/3 creature copies of the target land with vigilance and menace")
    void createsThreeCreatureCopiesOfTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castRebuildTheCity(mountain);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().hasType(CardType.LAND)).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
            assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
            assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
            assertThat(token.isSummoningSick()).isTrue();
        });
    }

    @Test
    @DisplayName("Can target only a land")
    void cannotTargetNonland() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RebuildTheCity()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Copies an opponent's land under the spell controller's control")
    void copiesOpponentsLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castRebuildTheCity(mountain);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allSatisfy(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(mountain);
    }

    @Test
    @DisplayName("Copies of a tapped Mountain enter untapped")
    void doesNotCopyTappedStatus() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();

        castRebuildTheCity(mountain);

        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(3)
                .allSatisfy(token -> assertThat(token.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Creates no tokens if the target land leaves before resolution")
    void createsNoTokensWhenTargetLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new RebuildTheCity()));
        addMana();
        harness.castSorcery(player1, 0, mountain.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        gd.playerGraveyards.get(player1.getId()).add(mountain.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rebuild the City");
    }

    @Test
    @DisplayName("Copied Mountain mana abilities respect summoning sickness")
    void copiedLandManaAbilityRespectsSummoningSickness() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castRebuildTheCity(mountain);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        Permanent token = gd.playerBattlefields.get(player1.getId()).get(1);
        token.setSummoningSick(false);
        harness.tapPermanent(player1, 1);
        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can copy a land token created by Rebuild the City")
    void copiesExistingLandToken() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castRebuildTheCity(mountain);
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).get(1);

        castRebuildTheCity(originalToken);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(6)
                .allSatisfy(token -> {
                    assertThat(token.getCard().hasType(CardType.LAND)).isTrue();
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(3);
                    assertThat(token.getEffectiveToughness()).isEqualTo(3);
                    assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
                    assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
                });
    }

    private void castRebuildTheCity(Permanent target) {
        harness.setHand(player1, List.of(new RebuildTheCity()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
