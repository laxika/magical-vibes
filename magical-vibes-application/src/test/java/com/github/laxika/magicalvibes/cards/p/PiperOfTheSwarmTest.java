package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({PiperOfTheSwarm.class, GrizzlyBears.class})
class PiperOfTheSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Rats you control have menace")
    void ratsYouControlHaveMenace() {
        addPiperReady(player1);
        Permanent rat = addRat(player1);
        Permanent nonRat = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRat = addRat(player2);

        assertThat(gqs.hasKeyword(gd, rat, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonRat, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentRat, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The token ability creates a 1/1 black Rat token")
    void createsRatToken() {
        Permanent piper = addPiperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, piper), 0, null, null);
        harness.passBothPriorities();

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getEffectivePower()).isEqualTo(1);
        assertThat(rat.getEffectiveToughness()).isEqualTo(1);
        assertThat(rat.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(rat.getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(gqs.hasKeyword(gd, rat, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing three Rats permanently gains control of a target creature")
    void sacrificesThreeRatsToGainControl() {
        Permanent piper = addPiperReady(player1);
        addRat(player1);
        addRat(player1);
        addRat(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, piper), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addPiperReady(Player player) {
        return addCreatureReady(player, new PiperOfTheSwarm());
    }

    private Permanent addRat(Player player) {
        Card card = new Card();
        card.setName("Rat");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.RAT));
        return addCreatureReady(player, card);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
