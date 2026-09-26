package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Befoul;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrippingTongueZubera.class, Befoul.class, SakuraTribeElder.class})
class DrippingTongueZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Spirit token for each Zubera that died this turn")
    void createsSpiritTokenForEachZuberaThatDiedThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DrippingTongueZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DrippingTongueZubera());
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul(), new Befoul(), new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        assertThat(spiritTokens()).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, second.getId());
        resolveAllTriggers();
        assertThat(spiritTokens()).isEqualTo(3);

        harness.castAndResolveSorcery(player1, 0, nonZubera.getId());
        assertThat(spiritTokens()).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts Zubera deaths under either player's control")
    void countsZuberaDeathsAcrossPlayers() {
        Permanent ownZubera = harness.addToBattlefieldAndReturn(player1, new DrippingTongueZubera());
        Permanent opposingZubera = harness.addToBattlefieldAndReturn(player2, new DrippingTongueZubera());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul(), new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveSorcery(player1, 0, opposingZubera.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, ownZubera.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates colorless 1/1 Spirit creatures")
    void createsColorlessOneOneSpiritCreatures() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new DrippingTongueZubera());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, zubera.getId());
        resolveAllTriggers();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getColor()).isNull();
        assertThat(spirit.getCard().getColors()).isEmpty();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
    }

    private long spiritTokens() {
        return countPermanents(player2, "Spirit");
    }
}
