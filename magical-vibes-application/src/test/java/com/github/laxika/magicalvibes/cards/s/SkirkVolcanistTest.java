package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WirewoodGuardian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirkVolcanist.class, Mountain.class, Forest.class, GoblinBrigand.class, WirewoodGuardian.class, Shock.class})
class SkirkVolcanistTest extends BaseCardTest {

    @Test
    void turningFaceUpSacrificesTwoMountainsAndDividesDamageAmongThreeCreatures() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));

        assertThat(volcanist.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(mountain1, mountain2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(mountain1.getCard(), mountain2.getCard());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstTarget.getId(), secondTarget.getId(), thirdTarget.getId());
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, thirdTarget.getId());

        PendingInteraction.ColorChoice firstDamageChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstDamageChoice).isNotNull();
        assertThat(firstDamageChoice.options()).containsExactly("1");
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");

        assertThat(firstTarget.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(thirdTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void turningFaceUpWithOneTargetDealsAllThreeDamageToIt() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WirewoodGuardian());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));

        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.ColorChoice damageChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(damageChoice).isNotNull();
        assertThat(damageChoice.options()).containsExactly("3");
        harness.handleListChoice(player1, "3");

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void turningFaceUpWithTwoTargetsAllowsChoosingTwoAndOneDamage() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));

        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.ColorChoice firstDamageChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstDamageChoice).isNotNull();
        assertThat(firstDamageChoice.options()).containsExactly("1", "2");
        assertThatThrownBy(() -> harness.handleListChoice(player1, "0"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "2");

        PendingInteraction.ColorChoice secondDamageChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondDamageChoice).isNotNull();
        assertThat(secondDamageChoice.options()).containsExactly("1");
        harness.handleListChoice(player1, "1");

        harness.passBothPriorities();

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstTarget.getCard());
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTurnFaceUpBySacrificingANonMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent volcanist = castFaceDown();

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(volcanist.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(mountain, forest, volcanist);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SkirkVolcanist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Skirk Volcanist");
    }

    @Test
    void cannotPayMorphCostWithOneMountainOrTheSameMountainTwice() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent volcanist = castFaceDown();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(volcanist);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, index, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, index,
                List.of(mountain.getId(), mountain.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(volcanist.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain, volcanist);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsMountainToTurnFaceUp() {
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent volcanist = castFaceDown();

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(ownMountain.getId(), opposingMountain.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(volcanist.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownMountain, volcanist);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingMountain);
    }

    @Test
    void damageAssignedToADeadTargetIsNotRedistributed() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WirewoodGuardian());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, firstTarget.getId());
        harness.assertInGraveyard(player2, "Goblin Brigand");

        harness.passBothPriorities();

        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void triggerResolvesAfterVolcanistDies() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WirewoodGuardian());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "3");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, volcanist.getId());
        harness.assertInGraveyard(player1, "Skirk Volcanist");

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void triggerCanTargetItsSourceButCannotTargetLandsOrPlayers() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(volcanist.getId())
                .doesNotContain(opposingMountain.getId(), player2.getId());
        harness.handlePermanentChosen(player1, volcanist.getId());
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skirk Volcanist");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
