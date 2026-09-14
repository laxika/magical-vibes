package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CripplingFatigue;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.l.Liquify;
import com.github.laxika.magicalvibes.cards.p.PardicCollaborator;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overmaster.class, CripplingFatigue.class, FieryTemper.class,
        Liquify.class, ObsessiveSearch.class, PardicCollaborator.class, SengirVampire.class})
class OvermasterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        Card drawnCard = new PardicCollaborator();
        harness.setLibrary(player1, List.of(drawnCard));
        castOvermaster();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The next instant spell can't be countered")
    void nextInstantSpellCantBeCountered() {
        castOvermaster();

        FieryTemper fieryTemper = new FieryTemper();
        harness.setHand(player1, List.of(fieryTemper));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fieryTemper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Fiery Temper");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("The next sorcery spell can't be countered")
    void nextSorcerySpellCantBeCountered() {
        harness.setLibrary(player1, List.of(new PardicCollaborator(), new PardicCollaborator(), new PardicCollaborator()));
        castOvermaster();

        ObsessiveSearch obsessiveSearch = new ObsessiveSearch();
        harness.setHand(player1, List.of(obsessiveSearch));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, obsessiveSearch.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Obsessive Search");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("A creature spell doesn't consume the grant")
    void creatureSpellDoesNotConsumeGrant() {
        castOvermaster();

        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, new PardicCollaborator(), "{3}{R}");
        harness.passBothPriorities();

        FieryTemper fieryTemper = new FieryTemper();
        harness.setHand(player1, List.of(fieryTemper));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fieryTemper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Fiery Temper");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("Only the first instant or sorcery spell is protected")
    void onlyFirstMatchingSpellIsProtected() {
        castOvermaster();

        FieryTemper firstSpell = new FieryTemper();
        harness.setHand(player1, List.of(firstSpell));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firstSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        FieryTemper secondSpell = new FieryTemper();
        harness.setHand(player1, List.of(secondSpell));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, secondSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(secondSpell.getId()));
    }

    @Test
    @DisplayName("An opponent's spell does not consume the grant")
    void opponentsSpellDoesNotConsumeGrant() {
        castOvermaster();

        FieryTemper opponentsSpell = new FieryTemper();
        harness.setHand(player2, List.of(opponentsSpell));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        FieryTemper ownSpell = new FieryTemper();
        harness.setHand(player1, List.of(ownSpell));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ownSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsSpell);
        harness.assertInGraveyard(player1, "Fiery Temper");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("A spell cast again from the graveyard is no longer protected")
    void recastSpellDoesNotKeepUncounterableGrant() {
        castOvermaster();

        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        CripplingFatigue cripplingFatigue = new CripplingFatigue();
        harness.setHand(player1, List.of(cripplingFatigue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0, firstTarget.getId());
        harness.passBothPriorities();

        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        int graveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(cripplingFatigue);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, graveyardIndex, secondTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cripplingFatigue.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(secondTarget.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cripplingFatigue.getId()));
    }

    @Test
    @DisplayName("The grant expires at the end of the turn")
    void grantExpiresAtEndOfTurn() {
        castOvermaster();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        ObsessiveSearch obsessiveSearch = new ObsessiveSearch();
        harness.setHand(player1, List.of(obsessiveSearch));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, obsessiveSearch.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(obsessiveSearch.getId()));
    }

    private void castOvermaster() {
        harness.castFromHand(player1, new Overmaster(), "{R}");
        harness.passBothPriorities();
    }
}
