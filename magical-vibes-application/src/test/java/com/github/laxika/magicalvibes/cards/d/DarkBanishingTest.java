package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.IgneousGolem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkBanishing.class, FemerefScouts.class, FeralShadow.class, Forest.class, IgneousGolem.class})
class DarkBanishingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Banishing targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent scouts = harness.addToBattlefieldAndReturn(player2, new FemerefScouts());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, scouts.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(scouts.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new FeralShadow());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Resolving Dark Banishing destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent scouts = harness.addToBattlefieldAndReturn(player2, new FemerefScouts());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
        harness.assertInGraveyard(player2, "Femeref Scouts");
    }

    @Test
    @DisplayName("Dark Banishing destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent scouts = harness.addToBattlefieldAndReturn(player2, new FemerefScouts());
        scouts.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
        harness.assertInGraveyard(player2, "Femeref Scouts");
    }

    @Test
    @DisplayName("Dark Banishing fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent scouts = harness.addToBattlefieldAndReturn(player2, new FemerefScouts());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, scouts.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Banishing");
    }

    @Test
    @DisplayName("Can target a nonblack creature you control")
    void canTargetOwnNonblackCreature() {
        Permanent scouts = harness.addToBattlefieldAndReturn(player1, new FemerefScouts());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Femeref Scouts");
        harness.assertInGraveyard(player1, "Femeref Scouts");
    }

    @Test
    @DisplayName("Can target a colorless creature")
    void canTargetColorlessCreature() {
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new IgneousGolem());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Igneous Golem");
        harness.assertInGraveyard(player2, "Igneous Golem");
    }
}
