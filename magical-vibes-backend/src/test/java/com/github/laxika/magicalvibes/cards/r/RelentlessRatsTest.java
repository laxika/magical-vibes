package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelentlessRatsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameQueryService gqs;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gqs = harness.getGameQueryService();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Relentless Rats has correct card properties")
    void hasCorrectProperties() {
        RelentlessRats card = new RelentlessRats();

        assertThat(card.getName()).isEqualTo("Relentless Rats");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.RAT);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostByOtherCreaturesWithSameNameEffect.class);
    }

    @Test
    @DisplayName("Relentless Rats is 2/2 when no other Relentless Rats are on the battlefield")
    void isBaseStatsWithNoOtherRats() {
        Permanent rats = addRatsReady(player1);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Relentless Rats gets +1/+1 for each other Relentless Rats you control")
    void countsOwnOtherRats() {
        Permanent rats = addRatsReady(player1);
        harness.addToBattlefield(player1, new RelentlessRats());
        harness.addToBattlefield(player1, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Relentless Rats counts other Relentless Rats controlled by opponents too")
    void countsOpponentRatsToo() {
        Permanent rats = addRatsReady(player1);
        harness.addToBattlefield(player2, new RelentlessRats());
        harness.addToBattlefield(player2, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Relentless Rats does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent rats = addRatsReady(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Relentless Rats bonus updates when other Relentless Rats leave the battlefield")
    void bonusUpdatesWhenOtherRatsLeave() {
        Permanent rats = addRatsReady(player1);
        harness.addToBattlefield(player1, new RelentlessRats());
        harness.addToBattlefield(player2, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Relentless Rats"));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    private Permanent addRatsReady(Player player) {
        Permanent permanent = new Permanent(new RelentlessRats());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
