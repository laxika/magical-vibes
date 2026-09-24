package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuChameleon;
import com.github.laxika.magicalvibes.cards.n.NightscapeApprentice;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessSpite.class, Forest.class, KavuChameleon.class, NightscapeApprentice.class,
        YavimayaBarbarian.class})
class RecklessSpiteTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys both targeted nonblack creatures and its controller loses 5 life")
    void destroysBothTargetsAndLosesFive() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        harness.castAndResolveInstant(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Yavimaya Barbarian"))
                .hasSize(2);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Life loss still happens when one target is no longer on the battlefield")
    void losesLifeEvenWhenTargetGone() {
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        Permanent leavingCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        harness.castInstant(player1, 0, List.of(remainingCreature.getId(), leavingCreature.getId()));

        // One target leaves before resolution — the life loss still happens.
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(leavingCreature.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not destroy a target that becomes black before resolution")
    void doesNotDestroyTargetThatBecomesBlackBeforeResolution() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuChameleon());
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        harness.castInstant(player1, 0, List.of(kavu.getId(), barbarian.getId()));

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        harness.assertOnBattlefield(player2, "Kavu Chameleon");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not resolve or cause life loss when both targets become black")
    void doesNotResolveWhenBothTargetsBecomeBlackBeforeResolution() {
        Permanent firstKavu = harness.addToBattlefieldAndReturn(player2, new KavuChameleon());
        Permanent secondKavu = harness.addToBattlefieldAndReturn(player2, new KavuChameleon());
        prepareCast();

        harness.castInstant(player1, 0, List.of(firstKavu.getId(), secondKavu.getId()));

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Kavu Chameleon"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Reckless Spite");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NightscapeApprentice());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(blackCreature.getId(), nonblackCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(forest.getId(), nonblackCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Cannot be cast with only one target")
    void requiresTwoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void requiresDistinctTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows a regeneration shield to replace destruction of a target")
    void allowsRegeneration() {
        Permanent regeneratingCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        Permanent destroyedCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        regeneratingCreature.setRegenerationShield(1);
        prepareCast();

        harness.castAndResolveInstant(player1, 0,
                List.of(regeneratingCreature.getId(), destroyedCreature.getId()));

        harness.assertOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        harness.assertLife(player1, 15);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
    }
}
