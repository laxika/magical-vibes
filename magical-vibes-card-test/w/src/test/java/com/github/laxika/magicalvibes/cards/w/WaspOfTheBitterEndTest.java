package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasTheDeceiver;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaspOfTheBitterEnd.class, GrizzlyBears.class, NicolBolasTheDeceiver.class, Spellbook.class,
        Unsummon.class})
class WaspOfTheBitterEndTest extends BaseCardTest {

    private void castBolasTargeting(Permanent target) {
        harness.castFromHand(player1, new NicolBolasTheDeceiver(), "{5}{U}{B}{R}");
        harness.handlePermanentChosen(player1, target.getId());
    }

    @Test
    @DisplayName("Casting a Bolas planeswalker prompts a creature target for the trigger")
    void bolasSpellPromptsTarget() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new NicolBolasTheDeceiver(), "{5}{U}{B}{R}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting sacrifice destroys the targeted creature and sacrifices the Wasp")
    void acceptSacrificesAndDestroys() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBolasTargeting(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertInGraveyard(player1, "Wasp of the Bitter End");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may keeps both creatures")
    void declineKeepsBoth() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBolasTargeting(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-Bolas spells do not trigger")
    void nonBolasSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Wasp controlled by the opponent cannot be sacrificed to its old controller's trigger")
    void cannotSacrificeWaspAfterLosingControl() {
        Permanent wasp = harness.addToBattlefieldAndReturn(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBolasTargeting(bears);

        gd.playerBattlefields.get(player1.getId()).remove(wasp);
        gd.playerBattlefields.get(player2.getId()).add(wasp);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertOnBattlefield(player2, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning the Wasp to hand prevents the destruction")
    void absentWaspCannotPaySacrifice() {
        Permanent wasp = harness.addToBattlefieldAndReturn(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBolasTargeting(bears);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, wasp.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal target prevents the optional sacrifice")
    void absentTargetDoesNotSacrificeWasp() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBolasTargeting(bears);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Wasp may target itself and be sacrificed")
    void mayTargetItself() {
        Permanent wasp = harness.addToBattlefieldAndReturn(player1, new WaspOfTheBitterEnd());
        castBolasTargeting(wasp);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertInGraveyard(player1, "Wasp of the Bitter End");
    }

    @Test
    @DisplayName("An opponent casting Bolas does not trigger the Wasp")
    void opponentsBolasDoesNotTrigger() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NicolBolasTheDeceiver(), "{5}{U}{B}{R}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
