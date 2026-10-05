package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.CandlesOfLeng;
import com.github.laxika.magicalvibes.cards.g.GriffinGuide;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanGrip.class, CandlesOfLeng.class, GriffinGuide.class, AshcoatBear.class,
        ThinkTwice.class, PrismaticLens.class})
class KrosanGripTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Candles of Leng");
        harness.assertInGraveyard(player2, "Candles of Leng");
        harness.assertInGraveyard(player1, "Krosan Grip");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GriffinGuide());
        target.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Griffin Guide");
        harness.assertInGraveyard(player2, "Griffin Guide");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Split second prevents spells and non-mana activated abilities")
    void splitSecondPreventsSpellsAndNonManaAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Split second still allows mana abilities")
    void splitSecondStillAllowsManaAbilities() {
        Permanent lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Krosan Grip");
    }

    @Test
    @DisplayName("Split second stops restricting spells after Krosan Grip resolves")
    void spellsCanBeCastAfterResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.setLibrary(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0);

        harness.assertInHand(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Think Twice");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its caster")
    void destroysOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Prismatic Lens");
    }

    @Test
    @DisplayName("Split second does not counter an ability already on the stack")
    void previouslyActivatedAbilityStillResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setLibrary(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new KrosanGrip()));
        addMana();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Candles of Leng");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
