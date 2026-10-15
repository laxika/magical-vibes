package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.p.PollenRemedy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Singe.class, AncientSpider.class, CloudCover.class, PollenRemedy.class})
class SingeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and makes the target creature black until end of turn")
    void damagesAndChangesColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudCover());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetOwnCreatureWithoutAffectingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveColors(gd, other))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    void changesColorEvenWhenDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy(), new Singe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    void lethalDamagePutsCreatureIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        target.setMarkedDamage(4);
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ancient Spider");
        harness.assertInGraveyard(player2, "Ancient Spider");
        harness.assertInGraveyard(player1, "Singe");
    }

    @Test
    void doesNotResolveWhenTargetReturnsToHand() {
        harness.addToBattlefield(player2, new CloudCover());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Ancient Spider");
        harness.assertNotOnBattlefield(player2, "Ancient Spider");
        harness.assertInGraveyard(player1, "Singe");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
