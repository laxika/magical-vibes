package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
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

@CardUsed({QuinaQuGourmet.class, BladeSplicer.class, WilyGoblin.class})
class QuinaQuGourmetTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one Frog to a creature-token creation event")
    void addsFrogToCreatureTokenCreation() {
        harness.addToBattlefield(player1, new QuinaQuGourmet());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Frog")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Frog puts a +1/+1 counter on Quina")
    void sacrificesFrogForCounter() {
        Permanent quina = harness.addToBattlefieldAndReturn(player1, new QuinaQuGourmet());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent frog = findPermanent(player1, "Frog");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, battlefieldIndex(quina), 0, null, null);
        harness.handlePermanentChosen(player1, frog.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(frog);
        assertThat(quina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
