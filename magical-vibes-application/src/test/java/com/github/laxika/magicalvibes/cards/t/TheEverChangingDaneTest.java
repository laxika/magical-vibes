package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEverChangingDane.class, GrizzlyBears.class, LlanowarElves.class})
class TheEverChangingDaneTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of the creature sacrificed to its ability")
    void becomesCopyOfSacrificedCreature() {
        Permanent dane = addReady(new TheEverChangingDane());
        addReady(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(dane.getCard().getPower()).isEqualTo(2);
        assertThat(dane.getCard().getToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Retains the copy ability")
    void retainsCopyAbility() {
        Permanent dane = addReady(new TheEverChangingDane());
        addReady(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        addReady(new LlanowarElves());
        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Llanowar Elves");
        assertThat(dane.getCard().getPower()).isEqualTo(1);
        assertThat(dane.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires another creature to pay the activation cost")
    void requiresAnotherCreature() {
        Permanent dane = addReady(new TheEverChangingDane());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dane), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
