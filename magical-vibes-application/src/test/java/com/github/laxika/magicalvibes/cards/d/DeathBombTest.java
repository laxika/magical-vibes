package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathBombTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys a nonblack creature and its controller loses 2 life")
    void destroysTargetAndControllerLosesTwoLife() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(victim);

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot regenerate the destroyed creature")
    void cannotBeRegenerated() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        Permanent victim = new Permanent(new GrizzlyBears());
        victim.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(victim);

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        Permanent victim = new Permanent(new DrudgeSkeletons());
        gd.playerBattlefields.get(player2.getId()).add(victim);

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
