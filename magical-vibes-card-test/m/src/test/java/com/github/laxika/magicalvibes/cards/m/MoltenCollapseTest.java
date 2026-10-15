package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CavernOfSouls;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinTombRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrazcaPuzzleDoor;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.cards.t.TithingBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenCollapse.class, FountainOfYouth.class, GrizzlyBears.class,
        CavernOfSouls.class, GoblinTombRaider.class, OrazcaPuzzleDoor.class,
        QuintoriusKand.class, TithingBlade.class})
class MoltenCollapseTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroysTargetSmallNoncreatureNonlandPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void cannotChooseBothWithoutHavingDescended() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void havingDescendedAllowsBothModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void destroysTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new QuintoriusKand());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(planeswalker.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Quintorius Kand");
    }

    @Test
    void destroysNoncreaturePermanentWithManaValueExactlyOne() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Orazca Puzzle-Door");
    }

    @ParameterizedTest
    @MethodSource("illegalTargets")
    void rejectsTargetsOutsideTheSelectedMode(int mode, Card targetCard) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{mode}, List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Stream<Arguments> illegalTargets() {
        return Stream.of(
                Arguments.of(0, new OrazcaPuzzleDoor()),
                Arguments.of(1, new GoblinTombRaider()),
                Arguments.of(1, new CavernOfSouls()),
                Arguments.of(1, new TithingBlade())
        );
    }

    @Test
    void mayChooseOnlyOneModeAfterDescending() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinTombRaider());
        harness.addToBattlefield(player2, new OrazcaPuzzleDoor());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Tomb Raider");
        harness.assertOnBattlefield(player2, "Orazca Puzzle-Door");
    }

    @Test
    void destroyingOwnCreatureEnablesBothModesOnTheNextSpell() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinTombRaider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        harness.setHand(player1, List.of(new MoltenCollapse(), new MoltenCollapse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(ownCreature.getId()), List.of());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Goblin Tomb Raider");

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Tomb Raider");
        harness.assertInGraveyard(player2, "Orazca Puzzle-Door");
    }

    @Test
    void opponentsCreatureDyingAndOwnSorceryResolvingDoNotEnableBothModes() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GoblinTombRaider());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GoblinTombRaider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        harness.setHand(player1, List.of(new MoltenCollapse(), new MoltenCollapse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(firstCreature.getId()), List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(secondCreature.getId(), artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillDestroysCreatureWhenTheOtherTargetLeavesTheBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinTombRaider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        harness.setLibrary(player2, List.of(new GoblinTombRaider()));
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of());
        harness.activateAbility(player2, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Tomb Raider");
        harness.assertInGraveyard(player2, "Orazca Puzzle-Door");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
