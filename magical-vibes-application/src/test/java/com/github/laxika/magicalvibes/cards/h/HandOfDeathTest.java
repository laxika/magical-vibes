package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IgneousGolem;
import com.github.laxika.magicalvibes.cards.m.MuckRats;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
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

@CardUsed({HandOfDeath.class, IgneousGolem.class, MuckRats.class, Swamp.class, WildGriffin.class})
class HandOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Hand of Death targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new WildGriffin());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, griffin.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(griffin.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new WildGriffin());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MuckRats());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new WildGriffin());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Resolving Hand of Death destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new WildGriffin());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wild Griffin");
        harness.assertInGraveyard(player2, "Wild Griffin");
    }

    @Test
    @DisplayName("A regeneration shield saves the target from Hand of Death")
    void regenerationShieldSavesTarget() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new WildGriffin());
        griffin.setRegenerationShield(1);

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wild Griffin");
        harness.assertNotInGraveyard(player2, "Wild Griffin");
        assertThat(griffin.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Hand of Death fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new WildGriffin());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hand of Death");
    }

    @Test
    @DisplayName("Can target and destroy a nonblack creature you control")
    void canTargetOwnNonblackCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WildGriffin());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wild Griffin");
        harness.assertInGraveyard(player1, "Wild Griffin");
    }

    @Test
    @DisplayName("Can target and destroy a colorless creature")
    void canTargetColorlessCreature() {
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new IgneousGolem());

        harness.setHand(player1, List.of(new HandOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, golem.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Igneous Golem");
        harness.assertInGraveyard(player2, "Igneous Golem");
    }
}
