package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SteelfinWhale;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtheriumSpinner.class, HillGiant.class, GrizzlyBears.class, SteelfinWhale.class})
class EtheriumSpinnerTest extends BaseCardTest {

    @Test
    void castingSpellWithManaValueFourCreatesFlyingThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void castingSpellWithManaValueLessThanFourDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isZero();
    }

    @Test
    void thopterIsCreatedBeforeTheQualifyingSpellResolves() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Thopter")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Hill Giant")).isZero();
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
    }

    @Test
    void opponentsQualifyingSpellDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Hill Giant")).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isZero();
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    void eachSpinnerCreatesItsOwnThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(2);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
    }

    @Test
    void everyQualifyingSpellInTheSameTurnCreatesThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new HillGiant(), new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(2);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(2);
    }

    @Test
    void manaValueAboveFourTriggersEvenWhenAffinityReducesTheCostBelowFour() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new SteelfinWhale()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(3);
        assertThat(countPermanents(player1, "Steelfin Whale")).isEqualTo(1);
    }
}
