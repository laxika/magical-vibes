package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaEvangel.class, GrizzlyBears.class})
class SelesnyaEvangelTest extends BaseCardTest {

    @Test
    void tapsSourceAndAnotherCreatureToCreateSaproling() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new SelesnyaEvangel());
        evangel.setSummoningSick(false);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(evangel.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    void cannotActivateWithoutAnotherUntappedCreature() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new SelesnyaEvangel());
        evangel.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
