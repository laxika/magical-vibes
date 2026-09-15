package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfUgin.class, BlightsteelColossus.class, DarksteelColossus.class})
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

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
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
    @DisplayName("Spells below mana value 7 and colored spells do not trigger")
    void doesNotTriggerForIneligibleSpells() {
        harness.addToBattlefield(player1, new SanctumOfUgin());
        Card lowValueSpell = spell("Low-value spell", "{6}", null);
        harness.setHand(player1, List.of(lowValueSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sanctum of Ugin");
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
