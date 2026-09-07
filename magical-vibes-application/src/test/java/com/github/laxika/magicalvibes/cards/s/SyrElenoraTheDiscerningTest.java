package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({SyrElenoraTheDiscerning.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class SyrElenoraTheDiscerningTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in hand while toughness stays 4")
    void powerEqualsHandSizeAndToughnessStaysFour() {
        Permanent elenora = addElenora(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        gd.playerHands.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));
        gd.playerHands.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, elenora)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elenora)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters-the-battlefield ability draws a card")
    void etbDrawsACard() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new SyrElenoraTheDiscerning()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Opponent's spell targeting Syr Elenora costs 2 more")
    void opponentSpellTargetingElenoraCostsMore() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elenora.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Syr Elenora does not tax its controller's spell")
    void ownSpellTargetingElenoraIsNotTaxed() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, elenora.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addElenora(Player player) {
        Permanent permanent = new Permanent(new SyrElenoraTheDiscerning());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
