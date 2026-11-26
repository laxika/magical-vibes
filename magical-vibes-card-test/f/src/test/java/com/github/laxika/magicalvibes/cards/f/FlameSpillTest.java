package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OboshThePreypiercer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameSpill.class, GrizzlyBears.class, ColossalDreadmaw.class,
        DarksteelMyr.class, OboshThePreypiercer.class})
class FlameSpillTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a 2/2 and the 2 excess to its controller")
    void dealsExcessDamageToTargetCreatureController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals no excess when the creature needs all 4 damage for lethal")
    void dealsNoExcessDamageWhenTargetNeedsAllFourDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rejects a player as the target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new FlameSpill()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage ignores indestructible: a 0/1 indestructible creature is dealt 1, its controller 3")
    void redirectsExcessInsteadOfMarkingItOnIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A creature already dealt lethal damage is dealt none, its controller all 4")
    void redirectsAllDamageWhenIndestructibleCreatureAlreadyHasLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The excess is worked out before a damage-increasing replacement applies")
    void determinesExcessBeforeApplyingOboshDamageMultiplier() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboshThePreypiercer());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Obosh, the Preypiercer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The excess goes to the creature's controller, the caster too")
    void dealsExcessToOwnCreatureController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OboshThePreypiercer());
        target.setMarkedDamage(4);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Obosh, the Preypiercer");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention of the damage to the controller leaves the creature's lethal damage dealt")
    void preventionOfThePlayersDamageLeavesTheCreaturesDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addPlayerDamagePreventionShield(player2.getId(), 2);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature that is also a planeswalker takes the greater excess: over its loyalty, not its toughness")
    void creaturePlaneswalkerTakesTheGreatestExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, creaturePlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 1);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        // 4 damage to a 2/3 with loyalty 1: 1 excess as a creature, 3 as a planeswalker; the greatest is 3 (CR 120.4a).
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Animated Walker");
    }

    private static Card creaturePlaneswalker() {
        Card card = new Card();
        card.setName("Animated Walker");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of(CardType.PLANESWALKER));
        card.setManaCost("{2}");
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(3);
        return card;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
