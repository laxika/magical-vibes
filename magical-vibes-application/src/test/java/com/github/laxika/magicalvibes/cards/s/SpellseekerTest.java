package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellseeker.class, LightningBolt.class, Divination.class, GrizzlyBears.class, Island.class})
class SpellseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may ability offers only instant or sorcery cards with mana value 2 or less")
    void acceptingMayOffersMatchingCards() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt");
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand")
    void choosingMatchingCardPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(true);

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Lightning Bolt");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Spellseeker()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new LightningBolt(),
                new Divination(),
                new GrizzlyBears(),
                new Island()));
    }

    private void resolveMayAbility(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
