package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalStingFaerie.class, IndomitableAncients.class, CloakAndDagger.class})
class FinalStingFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target creature that was dealt damage this turn")
    void etbDestroysCreatureDealtDamageThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new FinalStingFaerie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(target.getId());

        // Resolve ETB → destroys the target
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        harness.assertInGraveyard(player2, "Indomitable Ancients");
    }

    @Test
    @DisplayName("ETB can target own creature that was dealt damage this turn")
    void etbCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new FinalStingFaerie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Indomitable Ancients");
        harness.assertInGraveyard(player1, "Indomitable Ancients");
    }

    @Test
    @DisplayName("Cannot target creature that was not dealt damage this turn")
    void cannotTargetCreatureNotDealtDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());

        harness.setHand(player1, List.of(new FinalStingFaerie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it was dealt damage this turn")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloakAndDagger());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new FinalStingFaerie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new FinalStingFaerie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Final-Sting Faerie");
        assertThat(gd.stack).isEmpty();
    }
}
