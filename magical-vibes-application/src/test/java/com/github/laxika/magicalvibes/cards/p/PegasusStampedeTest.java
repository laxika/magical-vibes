package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PegasusStampede.class, Forest.class})
class PegasusStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Pegasus token with flying")
    void createsPegasusToken() {
        Permanent land = addLand(player1);
        cast(false, land);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = findPermanent(player1, "Pegasus");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        harness.assertInGraveyard(player1, "Pegasus Stampede");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can decline buyback without controlling a land")
    void canDeclineBuybackWithoutLand() {
        harness.castFromHand(player1, new PegasusStampede(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pegasus Stampede");
        assertThat(findPermanents(player1, "Pegasus")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a white Pegasus creature token")
    void createsWhitePegasusCreatureToken() {
        Permanent land = addLand(player1);
        cast(false, land);

        Permanent token = findPermanent(player1, "Pegasus");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.PEGASUS);
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land and returns Pegasus Stampede to hand")
    void buybackSacrificesLandAndReturnsToHand() {
        Permanent land = addLand(player1);
        cast(true, land);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(land::equals);
        assertThat(findPermanent(player1, "Pegasus")).satisfies(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        });
        harness.assertInHand(player1, "Pegasus Stampede");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Pegasus Stampede");
    }

    @Test
    @DisplayName("Buyback cannot be paid without sacrificing a land")
    void buybackRequiresLand() {
        harness.setHand(player1, List.of(new PegasusStampede()));
        addManaForSpell(player1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrificeAndBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a land controlled by another player")
    void buybackRequiresLandControlledByCaster() {
        Permanent opponentLand = addLand(player2);
        harness.setHand(player1, List.of(new PegasusStampede()));
        addManaForSpell(player1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrificeAndBuyback(
                player1, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Pegasus Stampede");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void cast(boolean buyback, Permanent land) {
        if (buyback) {
            harness.setHand(player1, List.of(new PegasusStampede()));
            addManaForSpell(player1);
            harness.castSorceryWithSacrificeAndBuyback(player1, 0, land.getId());
        } else {
            harness.castFromHand(player1, new PegasusStampede(), "{1}{W}");
        }
        harness.passBothPriorities();
    }

    private void addManaForSpell(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

}
