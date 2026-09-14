package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogWitch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeverSoul.class, SaprazzanRaider.class, BogWitch.class, Swamp.class})
class SeverSoulTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player1, List.of(new SeverSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys a nonblack creature and gains life equal to its toughness")
    void destroysAndGainsLife() {
        Permanent target = addCreatureReady(player2, new SaprazzanRaider()); // blue 1/2

        giveSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Saprazzan Raider");
        harness.assertInGraveyard(player2, "Saprazzan Raider");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains life equal to the target's effective toughness")
    void gainsLifeEqualToEffectiveToughness() {
        Permanent target = addCreatureReady(player2, new SaprazzanRaider());
        target.setToughnessModifier(3); // 2 + 3 = 5 effective toughness
        harness.setLife(player1, 10);

        giveSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player2, "Saprazzan Raider");
    }

    @Test
    @DisplayName("Destroyed creature can't be regenerated")
    void cannotBeRegenerated() {
        Permanent target = addCreatureReady(player2, new SaprazzanRaider());
        target.setRegenerationShield(1);

        giveSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Saprazzan Raider");
        harness.assertInGraveyard(player2, "Saprazzan Raider");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // A valid nonblack target so the spell itself is castable.
        addCreatureReady(player1, new SaprazzanRaider());

        Permanent witch = addCreatureReady(player2, new BogWitch()); // black creature

        giveSpell();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, witch.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new SaprazzanRaider());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        giveSpell();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Fizzles without gaining life if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new SaprazzanRaider());
        harness.setLife(player1, 20);

        giveSpell();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
