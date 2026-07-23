package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalDenizenTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    private Permanent denizen(Player owner) {
        UUID id = harness.getPermanentId(owner, "Infernal Denizen");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("With two Swamps, both are sacrificed and Denizen stays untapped")
    void sacrificesTwoSwampsNoPenalty() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → auto-sacrifice both

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(denizen(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("With fewer than two Swamps, Denizen taps and opponent may steal a creature")
    void cannotSacrificeTapsAndOffersOpponentSteal() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve → penalty

        assertThat(denizen(player1).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp")); // the one Swamp is not sacrificed

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Opponent may decline the steal after Denizen taps")
    void opponentMayDeclineSteal() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(denizen(player1).isTapped()).isTrue();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("{T}: gains control of target creature while Denizen remains on the battlefield")
    void tapAbilityStealsCreature() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        Permanent denizen = denizen(player1);
        denizen.setSummoningSick(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int denizenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(denizen);
        harness.activateAbility(player1, denizenIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(denizen.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Stolen creature returns when Denizen leaves the battlefield")
    void controlEndsWhenDenizenLeaves() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        Permanent denizen = denizen(player1);
        denizen.setSummoningSick(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int denizenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(denizen);
        harness.activateAbility(player1, denizenIdx, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, denizen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(denizen(player1).isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp"));
    }
}
