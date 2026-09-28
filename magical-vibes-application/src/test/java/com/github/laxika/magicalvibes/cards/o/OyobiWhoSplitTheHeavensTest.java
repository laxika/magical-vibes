package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OyobiWhoSplitTheHeavens.class, KamiOfFalseHope.class, VitalSurge.class, FrostOgre.class})
class OyobiWhoSplitTheHeavensTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell creates a 3/3 white Spirit token with flying")
    void spiritSpellCreatesToken() {
        addOyobi();
        prepareMainPhase();
        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");

        harness.passBothPriorities();

        List<Permanent> tokens = spiritTokens();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell creates a Spirit token")
    void arcaneSpellCreatesToken() {
        addOyobi();
        prepareMainPhase();
        harness.castFromHand(player1, new VitalSurge(), "{1}{G}");

        harness.passBothPriorities();

        assertThat(spiritTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane creates no token")
    void unrelatedSpellCreatesNoToken() {
        addOyobi();
        prepareMainPhase();
        harness.castFromHand(player1, new FrostOgre(), "{3}{R}{R}");

        harness.passBothPriorities();

        assertThat(spiritTokens()).isEmpty();
    }

    @Test
    @DisplayName("Casting a Spirit spell by an opponent creates no token")
    void opponentSpiritSpellCreatesNoToken() {
        addOyobi();
        prepareMainPhase(player2);
        harness.castFromHand(player2, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();

        assertThat(spiritTokens()).isEmpty();
    }

    private List<Permanent> spiritTokens() {
        return findPermanents(player1, "Spirit");
    }

    private void addOyobi() {
        harness.addToBattlefield(player1, new OyobiWhoSplitTheHeavens());
    }

    private void prepareMainPhase() {
        prepareMainPhase(player1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
