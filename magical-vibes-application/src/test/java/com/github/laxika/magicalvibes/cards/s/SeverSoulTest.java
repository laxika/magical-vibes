package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.b.BogWitch;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeverSoul.class, BogImp.class, Forest.class, GrizzlyBears.class, SaprazzanRaider.class, BogWitch.class, Swamp.class})
class SeverSoulTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player1, List.of(new SeverSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys a nonblack creature and gains life equal to its toughness")
    void destroysAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // green 2/2

        giveSpell();
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Destroyed creature can't be regenerated")
    void cannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        giveSpell();
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        // A valid nonblack creature so the spell itself is castable.
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        giveSpell();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // A valid nonblack target so the spell itself is castable.
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent imp = harness.addToBattlefieldAndReturn(player2, new BogImp()); // black creature

        giveSpell();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, imp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains life equal to the target's effective toughness")
    void gainsLifeEqualToEffectiveToughness() {
        harness.setLife(player1, 10);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setToughnessModifier(3); // 2 + 3 = 5 effective toughness

        giveSpell();
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fizzles without gaining life if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.setLife(player1, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        giveSpell();
        harness.castSorcery(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Sever Soul");
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
}
