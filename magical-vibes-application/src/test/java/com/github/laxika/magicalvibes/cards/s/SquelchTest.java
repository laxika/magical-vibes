package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frostwielder;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.cards.m.MinamoSchoolAtWatersEdge;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Squelch.class, Frostwielder.class, ReachThroughMists.class, MinamoSchoolAtWatersEdge.class,
        KamiOfTheHunt.class})
class SquelchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and draws a card")
    void countersActivatedAbilityAndDraws() {
        Permanent frostwielder = addCreatureReady(player2, new Frostwielder());

        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, frostwielder.getCard().getId());

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
        // Squelch left the hand, then drew one card: net hand size is unchanged minus the cast card.
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        ReachThroughMists reachThroughMists = new ReachThroughMists();
        harness.setHand(player2, List.of(reachThroughMists));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, reachThroughMists.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        Permanent minamo = harness.addToBattlefieldAndReturn(player2, new MinamoSchoolAtWatersEdge());

        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.passPriority(player2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, minamo.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter its controller's own activated ability without undoing its cost")
    void countersOwnActivatedAbility() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());
        harness.setHand(player1, List.of(new Squelch()));
        harness.setLibrary(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.castAndResolveInstant(player1, 0, abilityId);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Frostwielder");
        harness.assertNotInGraveyard(player1, "Frostwielder");
        harness.assertInHand(player1, "Reach Through Mists");
        assertThat(frostwielder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when its only target has already been countered")
    void doesNotDrawWhenTargetLeavesStack() {
        addCreatureReady(player1, new Frostwielder());
        harness.setHand(player1, List.of(new Squelch(), new Squelch()));
        harness.setLibrary(player1, List.of(new ReachThroughMists(), new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.castInstant(player1, 0, abilityId);
        harness.castAndResolveInstant(player1, 0, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a triggered ability")
    void cannotTargetTriggeredAbility() {
        harness.addToBattlefield(player2, new KamiOfTheHunt());
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0);
        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst().orElseThrow().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }
}
