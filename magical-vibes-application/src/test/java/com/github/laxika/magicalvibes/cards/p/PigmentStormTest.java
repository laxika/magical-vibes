package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PigmentStorm.class, GrizzlyBears.class, SpinedKarok.class})
class PigmentStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a creature and excess damage to its controller")
    void dealsExcessDamageToController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PigmentStorm()));
        addPigmentStormMana();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals no excess damage when the creature has enough toughness")
    void dealsNoExcessDamageWhenCreatureSurvives() {
        harness.addToBattlefield(player2, createCreature("Large Beast", 5, 6));
        harness.setHand(player1, List.of(new PigmentStorm()));
        addPigmentStormMana();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Large Beast");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Large Beast");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new PigmentStorm()));
        addPigmentStormMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redirectsExcessInsteadOfDealingItToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new PigmentStorm()));
        harness.setLife(player2, 20);
        addPigmentStormMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Spined Karok");
        harness.assertLife(player2, 19);
    }

    @Test
    void determinesExcessBeforeCreatureDamagePrevention() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        target.setDamagePreventionShield(4);
        harness.setHand(player1, List.of(new PigmentStorm()));
        harness.setLife(player2, 20);
        addPigmentStormMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Spined Karok");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    void accountsForMarkedDamageAndDamagesOwnCreatureController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        target.setMarkedDamage(2);
        harness.setHand(player1, List.of(new PigmentStorm()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPigmentStormMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Spined Karok");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsNoDamageWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new PigmentStorm()));
        harness.setLife(player2, 20);
        addPigmentStormMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Pigment Storm");
    }

    private void addPigmentStormMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
