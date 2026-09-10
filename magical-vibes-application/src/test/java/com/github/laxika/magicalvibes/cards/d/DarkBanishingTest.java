package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.p.PitImp;
import com.github.laxika.magicalvibes.cards.s.SoltariMonk;
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

@CardUsed({DarkBanishing.class, CanopySpider.class, Forest.class, MetallicSliver.class, PitImp.class,
        SoltariMonk.class})
class DarkBanishingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Banishing targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, spider.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(spider.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new PitImp());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a creature with protection from black")
    void cannotTargetCreatureWithProtectionFromBlack() {
        Permanent monk = harness.addToBattlefieldAndReturn(player2, new SoltariMonk());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, monk.getId()))
                .isInstanceOf(IllegalStateException.class);
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
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertNotOnBattlefield(player2, "Canopy Spider");
        harness.assertInGraveyard(player2, "Canopy Spider");
    }

    @Test
    @DisplayName("Dark Banishing destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());
        spider.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertNotOnBattlefield(player2, "Canopy Spider");
        harness.assertInGraveyard(player2, "Canopy Spider");
    }

    @Test
    @DisplayName("Dark Banishing fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, spider.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Banishing");
    }

    @Test
    @DisplayName("Can target a nonblack creature you control")
    void canTargetOwnNonblackCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertNotOnBattlefield(player1, "Canopy Spider");
        harness.assertInGraveyard(player1, "Canopy Spider");
    }

    @Test
    @DisplayName("Can target a colorless creature")
    void canTargetColorlessCreature() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player2, new MetallicSliver());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, sliver.getId());

        harness.assertNotOnBattlefield(player2, "Metallic Sliver");
        harness.assertInGraveyard(player2, "Metallic Sliver");
    }

    @Test
    @CardUsed(DarksteelMyr.class)
    @DisplayName("Dark Banishing cannot destroy an indestructible creature")
    void cannotDestroyIndestructibleCreature() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, myr.getId());

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
    }
}
