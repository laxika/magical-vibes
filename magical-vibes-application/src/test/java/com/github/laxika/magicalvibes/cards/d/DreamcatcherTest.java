package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreamcatcher.class, SpiritualVisit.class, ArabaMothrider.class})
class DreamcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger sacrifices Dreamcatcher and draws a card for an Arcane spell")
    void acceptingArcaneTriggerSacrificesAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Accepting the trigger sacrifices Dreamcatcher and draws a card for a Spirit spell")
    void acceptingSpiritTriggerSacrificesAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setHand(player1, List.of(new Dreamcatcher()));
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Declining the trigger keeps Dreamcatcher and draws no card")
    void decliningTriggerKeepsDreamcatcherAndDoesNotDraw() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInGraveyard(player1, "Dreamcatcher");
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Dreamcatcher")
    void unrelatedSpellDoesNotTrigger() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setHand(player1, List.of(new ArabaMothrider()));
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addDreamcatcher() {
        return harness.addToBattlefieldAndReturn(player1, new Dreamcatcher());
    }
}
