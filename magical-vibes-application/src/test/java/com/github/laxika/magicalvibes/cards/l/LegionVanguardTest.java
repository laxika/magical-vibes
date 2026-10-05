package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionVanguard.class, Forest.class, GrizzlyBears.class})
class LegionVanguardTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndPutsExploredLandIntoHand() {
        Permanent vanguard = addReadyVanguard();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificesAnotherCreatureAndExploringNonlandAddsCounter() {
        Permanent vanguard = addReadyVanguard();
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears exploredCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exploredCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(exploredCard.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutAnotherCreature() {
        addReadyVanguard();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVanguard() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new LegionVanguard());
        vanguard.setSummoningSick(false);
        return vanguard;
    }

    @Test
    void canPutExploredNonlandIntoGraveyard() {
        Permanent vanguard = addReadyVanguard();
        harness.addToBattlefield(player1, new LegionVanguard());
        LegionVanguard exploredCard = new LegionVanguard();
        harness.setLibrary(player1, List.of(exploredCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(vanguard);
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(exploredCard.getId());
    }

    @Test
    void exploringEmptyLibraryStillAddsCounter() {
        Permanent vanguard = addReadyVanguard();
        harness.addToBattlefield(player1, new LegionVanguard());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new LegionVanguard());
        vanguard.setSummoningSick(true);
        vanguard.setTapped(true);
        harness.addToBattlefield(player1, new LegionVanguard());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
        assertThat(vanguard.isTapped()).isTrue();
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addReadyVanguard();
        harness.addToBattlefield(player2, new LegionVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Legion Vanguard");
    }
}
