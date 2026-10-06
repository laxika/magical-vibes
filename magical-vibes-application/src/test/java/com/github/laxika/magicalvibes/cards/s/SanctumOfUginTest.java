package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.cards.b.BreakerOfArmies;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.e.EndlessOne;
import com.github.laxika.magicalvibes.cards.p.PlatedCrusher;
import com.github.laxika.magicalvibes.cards.u.UlamogsDespoiler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfUgin.class, BlightsteelColossus.class, DarksteelColossus.class,
        BreakerOfArmies.class, PlatedCrusher.class, UlamogsDespoiler.class, ScourFromExistence.class, EndlessOne.class})
class SanctumOfUginTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new SanctumOfUgin());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless spell with mana value 7 or greater may search for a colorless creature")
    void highManaValueColorlessSpellTriggersSearch() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        BlightsteelColossus found = new BlightsteelColossus();
        Card coloredCreature = spell("Colored creature", "{7}{R}", CardColor.RED);
        harness.setLibrary(player1, List.of(found, coloredCreature));
        harness.setHand(player1, List.of(new DarksteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertInGraveyard(player1, "Sanctum of Ugin");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(found);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
    }

    @Test
    @DisplayName("The controller may decline to sacrifice Sanctum of Ugin")
    void mayDeclineToSacrifice() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        BlightsteelColossus found = new BlightsteelColossus();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new DarksteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(found);
    }

    @Test
    @DisplayName("Spells below mana value 7 do not trigger")
    void doesNotTriggerForIneligibleSpells() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        Card lowValueSpell = spell("Low-value spell", "{6}", null);
        harness.setHand(player1, List.of(lowValueSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
    }

    @Test
    @DisplayName("A seven-mana colorless instant triggers and only colorless creatures can be found")
    void sevenManaNoncreatureSpellTriggers() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.addToBattlefield(player2, new BreakerOfArmies());
        BreakerOfArmies found = new BreakerOfArmies();
        harness.setLibrary(player1, List.of(found, new PlatedCrusher(), new ScourFromExistence()));
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Breaker of Armies"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player2, "Breaker of Armies");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(found);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        harness.assertNotOnBattlefield(player2, "Breaker of Armies");
    }

    @Test
    @DisplayName("A colored seven-mana creature does not trigger")
    void coloredSevenManaSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.setHand(player1, List.of(new PlatedCrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player1, "Plated Crusher");
    }

    @Test
    @DisplayName("A real six-mana colorless creature does not trigger")
    void sixManaColorlessCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.setHand(player1, List.of(new UlamogsDespoiler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player1, "Ulamog's Despoiler");
    }

    @Test
    @DisplayName("An opponent's qualifying spell does not trigger")
    void opponentsColorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.addToBattlefield(player1, new BreakerOfArmies());
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Breaker of Armies"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertNotOnBattlefield(player1, "Breaker of Armies");
    }

    @Test
    @DisplayName("Sacrificing Sanctum with an empty library does not prevent the triggering spell resolving")
    void emptyLibraryStillSacrificesSanctum() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BreakerOfArmies()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player1, "Breaker of Armies");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X counts toward mana value on the stack at the seven-mana threshold")
    void sevenManaXSpellTriggers() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.setHand(player1, List.of(new EndlessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0, 7);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player1, "Endless One");
    }

    @Test
    @DisplayName("An X spell with X equal to six does not trigger")
    void sixManaXSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        harness.setHand(player1, List.of(new EndlessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, 6);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
        harness.assertOnBattlefield(player1, "Endless One");
    }

    @Test
    @DisplayName("The controller may fail to find even when an eligible creature is in the library")
    void mayFailToFindEligibleCreature() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        BreakerOfArmies available = new BreakerOfArmies();
        harness.setLibrary(player1, List.of(available));
        harness.setHand(player1, List.of(new BreakerOfArmies()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum of Ugin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(available);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        harness.assertOnBattlefield(player1, "Breaker of Armies");
    }

    @Test
    @DisplayName("A departed Sanctum cannot be sacrificed to search")
    void departedSourceDoesNotSearch() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        BreakerOfArmies available = new BreakerOfArmies();
        harness.setLibrary(player1, List.of(available));
        harness.setHand(player1, List.of(new BreakerOfArmies()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(available);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        harness.assertOnBattlefield(player1, "Breaker of Armies");
    }

    private Card spell(String name, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        return card;
    }
}
