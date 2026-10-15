package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfDuplicity.class, GrizzlyBears.class, HillGiant.class, EdgarMarkov.class})
class VisionsOfDuplicityTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when both targets have the same controller")
    void doesNothingWhenBothTargetsHaveSameController() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Rejects a creature controlled by the spell's caster")
    void rejectsCreatureYouControl() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    @DisplayName("Flashback cost is reduced by the greatest owned commander")
    void flashbackUsesCommanderManaValue() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void exchangesCreaturesControlledByDifferentOpponents() {
        Player third = addThirdPlayer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new HillGiant());
        first.tap();
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertOnBattlefield(third, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(third, "Hill Giant");
        assertThat(first.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Visions of Duplicity");
    }

    @Test
    void noExchangeWhenOneTargetLeavesBeforeResolution() {
        Player third = addThirdPlayer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertOnBattlefield(third, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Visions of Duplicity");
    }

    @Test
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Visions of Duplicity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackUsesExactDiscountForOwnedCommanderUnderOpponentControl() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void commanderInGraveyardDoesNotReduceFlashbackCost() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell, commander));
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackWithoutCommanderPaysFullCostAndExilesSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void normalCastIsNotDiscountedByCommander() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Visions of Duplicity");
    }

    @Test
    void graveyardCastWithoutFlashbackHasNoCommanderDiscount() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell));

        gd.graveyardPlayPermissions.put(spell.getId(), player1.getId());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addManaForNormalCast();

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertInGraveyard(player1, "Visions of Duplicity");
    }

    private Player addThirdPlayer() {
        Player third = new Player(UUID.randomUUID(), "Charlie");
        gd.playerIds.add(third.getId());
        gd.orderedPlayerIds.add(third.getId());
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(third.getId(), "Charlie");
        gd.playerDecks.put(third.getId(), new ArrayList<>());
        gd.playerHands.put(third.getId(), new ArrayList<>());
        gd.playerGraveyards.put(third.getId(), new ArrayList<>());
        gd.playerBattlefields.put(third.getId(), new ArrayList<>());
        gd.playerManaPools.put(third.getId(), new ManaPool());
        gd.playerLifeTotals.put(third.getId(), 20);
        return third;
    }

    private void addManaForNormalCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
