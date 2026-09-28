package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FainTheBroker.class, GrizzlyBears.class, Spellbook.class})
class FainTheBrokerTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndPutsTwoCountersOnTargetCreature() {
        Permanent fain = addReadyFain();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void removesAnyCounterFromCreatureAndCreatesTreasure() {
        Permanent fain = addReadyFain();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(findPermanent(player1, "Treasure").getCard().isToken()).isTrue();
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void sacrificesArtifactAndCreatesFlyingInkling() {
        Permanent fain = addReadyFain();
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void paysManaToUntapFain() {
        Permanent fain = addReadyFain();
        fain.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(fain.isTapped()).isFalse();
    }

    private Permanent addReadyFain() {
        return addCreatureReady(player1, new FainTheBroker());
    }
}
