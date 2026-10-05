package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlchemistsGreeting;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.w.WhispersOfEmrakul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NephaliaAcademy.class, Catalog.class, Coercion.class, GrizzlyBears.class,
        RavenousRats.class, WhispersOfEmrakul.class, AlchemistsGreeting.class, ImprisonedInTheMoon.class})
class NephaliaAcademyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new NephaliaAcademy());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent-caused discard goes on top of the library")
    void opponentCausedDiscardGoesOnTopOfLibrary() {
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A self-caused discard still goes to the graveyard")
    void selfCausedDiscardStillGoesToGraveyard() {
        harness.addToBattlefield(player1, new NephaliaAcademy());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCausedDiscardMayGoToGraveyard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void randomDiscardMayGoToGraveyard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentSelectedDiscardMayGoToGraveyard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void acceptingReplacementForMadnessCardPreventsMadness() {
        AlchemistsGreeting discarded = new AlchemistsGreeting();
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discarded);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningReplacementForMadnessCardAllowsMadness() {
        AlchemistsGreeting discarded = new AlchemistsGreeting();
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void losingAbilitiesDisablesDiscardReplacement() {
        GrizzlyBears discarded = new GrizzlyBears();
        Permanent academy = harness.addToBattlefieldAndReturn(player2, new NephaliaAcademy());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new ImprisonedInTheMoon(), new RavenousRats()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, academy.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
