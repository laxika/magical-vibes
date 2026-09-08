package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfDeath.class, GrizzlyBears.class, BogImp.class, Forest.class})
class HandOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Hand of Death targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // A legal nonblack target so the spell itself is playable.
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent blackCreature = new Permanent(new BogImp());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blackCreature);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent forest = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(forest);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Resolving Hand of Death destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hand of Death respects a regeneration shield")
    void regenerationShieldSavesCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Hand of Death fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hand of Death");
    }
}
