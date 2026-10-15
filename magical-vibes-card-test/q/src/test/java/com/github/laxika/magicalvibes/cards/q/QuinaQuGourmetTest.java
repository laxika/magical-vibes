package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.r.RelmsSketching;
import com.github.laxika.magicalvibes.cards.s.ScorpionSentinel;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuinaQuGourmet.class, BladeSplicer.class, WilyGoblin.class,
        RelmsSketching.class, ScorpionSentinel.class})
class QuinaQuGourmetTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one Frog to a creature-token creation event")
    void addsFrogToCreatureTokenCreation() {
        harness.addToBattlefield(player1, new QuinaQuGourmet());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
        Permanent frog = findPermanent(player1, "Frog");
        assertThat(frog.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(frog.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(frog.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
        assertThat(frog.getEffectivePower()).isEqualTo(1);
        assertThat(frog.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds one Frog when a noncreature token is created")
    void addsFrogToNoncreatureTokenCreation() {
        harness.addToBattlefield(player1, new QuinaQuGourmet());
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Frog")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Frog puts a +1/+1 counter on Quina")
    void sacrificesFrogForCounter() {
        Permanent quina = harness.addToBattlefieldAndReturn(player1, new QuinaQuGourmet());
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent frog = findPermanent(player1, "Frog");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, battlefieldIndex(quina), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(frog);
        assertThat(quina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds a Frog when a token copy is created")
    void addsFrogToTokenCopyCreation() {
        harness.addToBattlefield(player1, new QuinaQuGourmet());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());
        harness.setHand(player1, List.of(new RelmsSketching()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, sentinel.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Scorpion Sentinel")).hasSize(2);
        assertThat(findPermanents(player1, "Frog")).hasSize(1);
    }

    @Test
    @DisplayName("Does not add a Frog to tokens created under an opponent's control")
    void doesNotAddFrogForOpponent() {
        harness.addToBattlefield(player1, new QuinaQuGourmet());
        harness.castFromHand(player2, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Frog")).isEmpty();
        assertThat(findPermanents(player2, "Frog")).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a Frog to sacrifice")
    void cannotActivateWithoutFrog() {
        Permanent quina = harness.addToBattlefieldAndReturn(player1, new QuinaQuGourmet());
        harness.addToBattlefield(player1, new ScorpionSentinel());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(quina), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
