package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FocusFire.class, AirElemental.class, GrizzlyBears.class})
class FocusFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals two damage when its controller controls no creatures or Spacecraft")
    void dealsBaseDamage() {
        Permanent target = addAttacker(new AirElemental());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals additional damage for each creature and Spacecraft its controller controls")
    void countsCreaturesAndSpacecraft() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, spacecraft(false));
        Permanent target = addAttacker(new AirElemental());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts a Spacecraft creature only once")
    void countsSpacecraftCreatureOnce() {
        harness.addToBattlefield(player1, spacecraft(true));
        Permanent target = addAttacker(new AirElemental());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new FocusFire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addAttacker(Card card) {
        harness.addToBattlefield(player2, card);
        Permanent target = findPermanent(player2, "Air Elemental");
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

    private Card spacecraft(boolean creature) {
        Card card = new Card();
        card.setName("Test Spacecraft");
        card.setType(creature ? CardType.CREATURE : CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.SPACECRAFT));
        if (creature) {
            card.setPower(1);
            card.setToughness(1);
        }
        return card;
    }
}
