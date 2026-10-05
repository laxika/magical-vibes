package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.Geistwave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheDance;
import com.github.laxika.magicalvibes.cards.s.SecretsOfTheKey;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LierDiscipleOfTheDrowned.class, Counterspell.class, GrizzlyBears.class, Shock.class,
        Geistwave.class, JoinTheDance.class, SecretsOfTheKey.class, TurnToFrog.class})
class LierDiscipleOfTheDrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cannot be countered while Lier is on the battlefield")
    void spellsCannotBeCountered() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        GrizzlyBears bears = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Counterspell");
    }

    @Test
    @DisplayName("Instant and sorcery cards in your graveyard have flashback")
    void grantsFlashbackToInstantAndSorceryCards() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Lier does not grant flashback to creature cards")
    void doesNotGrantFlashbackToCreatureCards() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lier grants an additional cheaper flashback cost to an instant with printed flashback")
    void grantsAdditionalFlashbackCostToInstant() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        SecretsOfTheKey secrets = new SecretsOfTheKey();
        harness.setGraveyard(player1, List.of(secrets));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Clue"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secrets);
    }

    @Test
    @DisplayName("Lier grants an additional cheaper flashback cost to a sorcery with printed flashback")
    void grantsAdditionalFlashbackCostToSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        JoinTheDance dance = new JoinTheDance();
        harness.setGraveyard(player1, List.of(dance));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Human"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dance);
    }

    @Test
    @DisplayName("Lier does not grant flashback to an opponent's graveyard")
    void doesNotGrantFlashbackToOpponent() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Geistwave wave = new Geistwave();
        harness.setGraveyard(player2, List.of(wave));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0,
                findPermanent(player1, "Lier, Disciple of the Drowned").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(wave);
    }

    @Test
    @DisplayName("Lier stops granting flashback when it loses all abilities")
    void losesFlashbackGrantWhenAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Permanent lier = findPermanent(player1, "Lier, Disciple of the Drowned");
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, lier.getId());
        harness.passBothPriorities();
        Geistwave wave = new Geistwave();
        harness.setGraveyard(player1, List.of(wave));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, lier.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wave);
    }

    @Test
    @DisplayName("A spell already cast with Lier's flashback is exiled even after Lier leaves")
    void flashbackExilesSpellAfterLierLeaves() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Permanent lier = findPermanent(player1, "Lier, Disciple of the Drowned");
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lier.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Lier, Disciple of the Drowned");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Lier stops granting flashback after leaving the battlefield")
    void stopsGrantingFlashbackAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        harness.addToBattlefield(player2, new LierDiscipleOfTheDrowned());
        Permanent lier = findPermanent(player1, "Lier, Disciple of the Drowned");
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, lier.getId());
        harness.passBothPriorities();
        Geistwave wave = new Geistwave();
        harness.setGraveyard(player1, List.of(wave));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.assertNotOnBattlefield(player1, "Lier, Disciple of the Drowned");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                findPermanent(player2, "Lier, Disciple of the Drowned").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wave);
    }

    @Test
    @DisplayName("Lier also protects its controller's noncreature spells from counters")
    void protectsControllersInstant() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("Lier can be countered before its static ability is active on the battlefield")
    void canBeCounteredWhileBeingCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LierDiscipleOfTheDrowned lier = new LierDiscipleOfTheDrowned();
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, lier, "{3}{U}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lier.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lier, Disciple of the Drowned");
        harness.assertInGraveyard(player1, "Lier, Disciple of the Drowned");
        harness.assertInGraveyard(player2, "Counterspell");
    }
}
