package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.cards.s.SerratedArrows;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Joven.class, DwarvenTrader.class, Roterothopter.class, SerratedArrows.class})
class JovenTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target noncreature artifact")
    void destroysNoncreatureArtifact() {
        addCreatureReady(player1, new Joven());
        harness.addToBattlefield(player2, new SerratedArrows());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent arrows = findPermanent(player2, "Serrated Arrows");
        harness.activateAbility(player1, 0, null, arrows.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serrated Arrows");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new Joven());
        harness.addToBattlefield(player2, new Roterothopter());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent thopter = findPermanent(player2, "Roterothopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        addCreatureReady(player1, new Joven());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwarvenTrader());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Tapping Joven is part of the activation cost")
    void tapsAsCost() {
        Permanent joven = addCreatureReady(player1, new Joven());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerratedArrows());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(joven.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate without three red mana")
    void cannotActivateWithoutEnoughRedMana() {
        addCreatureReady(player1, new Joven());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerratedArrows());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
