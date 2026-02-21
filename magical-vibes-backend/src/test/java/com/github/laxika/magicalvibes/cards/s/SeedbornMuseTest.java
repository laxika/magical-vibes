package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeedbornMuseTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Seedborn Muse has correct card properties")
    void hasCorrectProperties() {
        SeedbornMuse card = new SeedbornMuse();

        assertThat(card.getName()).isEqualTo("Seedborn Muse");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect.class);
    }

    @Test
    @DisplayName("Seedborn Muse untaps all your permanents during opponent's untap step")
    void untapsAllYourPermanentsOnOpponentsUntapStep() {
        Permanent muse = addReadySeedbornMuse(player1);
        Permanent bears = addReadyBears(player1);

        muse.tap();
        bears.tap();
        assertThat(muse.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1); // next active is player2

        assertThat(muse.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Seedborn Muse, non-active player's tapped permanents stay tapped")
    void withoutSeedbornMusePermanentsStayTapped() {
        Permanent bears = addReadyBears(player1);
        bears.tap();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1); // next active is player2

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seedborn Muse only untaps permanents its controller controls")
    void onlyControllerPermanentsUntap() {
        Permanent p1Muse = addReadySeedbornMuse(player1);
        Permanent p1Bears = addReadyBears(player1);
        Permanent p2Bears = addReadyBears(player2);

        p1Muse.tap();
        p1Bears.tap();
        p2Bears.tap();

        advanceToNextTurn(player1); // player2 untap step

        assertThat(p1Muse.isTapped()).isFalse();
        assertThat(p1Bears.isTapped()).isFalse();
        assertThat(p2Bears.isTapped()).isFalse(); // active player's normal untap
    }

    private Permanent addReadySeedbornMuse(Player player) {
        Permanent perm = new Permanent(new SeedbornMuse());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
