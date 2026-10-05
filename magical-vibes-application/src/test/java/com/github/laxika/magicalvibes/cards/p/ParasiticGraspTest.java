package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.a.AncientLumberknot;
import com.github.laxika.magicalvibes.cards.c.CloakedCadet;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParasiticGrasp.class, HealingSalve.class, AncientLumberknot.class,
        CloakedCadet.class, TravelingMinister.class})
class ParasiticGraspTest extends BaseCardTest {

    @Test
    void normalCastDamagesHumanAndGainsThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Human", CardSubtype.HUMAN));
        castNormal(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void normalCastCannotTargetNonHumanCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Bear", CardSubtype.BEAR));
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human creature");
    }

    @Test
    void cleaveCastDamagesAnyCreatureAndGainsThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Bear", CardSubtype.BEAR));
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void gainsThreeLifeEvenWhenDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Human", CardSubtype.HUMAN));
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castNormal(target);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    private void castNormal(Permanent target) {
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        addNormalMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    void normalCastCanDamageYourOwnHuman() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CloakedCadet());

        castNormal(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player1, "Cloaked Cadet");
    }

    @Test
    void lethalDamageStillGainsExactlyThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());

        castNormal(target);

        harness.assertInGraveyard(player2, "Traveling Minister");
        harness.assertNotOnBattlefield(player2, "Traveling Minister");
        harness.assertLife(player1, 23);
    }

    @Test
    void doesNotGainLifeWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new ParasiticGrasp(), new ParasiticGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Traveling Minister");
        harness.assertLife(player1, 23);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cleaveRequiresItsFullManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientLumberknot());
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Parasitic Grasp");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void cleaveDamagesNonHumanCreatureAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientLumberknot());
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ancient Lumberknot");
        harness.assertLife(player1, 23);
    }

    @Test
    void cleaveCannotTargetPlayer() {
        harness.setHand(player1, List.of(new ParasiticGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private static Card creatureWithSubtype(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(5);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
