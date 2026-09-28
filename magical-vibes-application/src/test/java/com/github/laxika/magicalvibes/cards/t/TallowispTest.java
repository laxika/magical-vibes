package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GenjuOfTheFields;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.p.PhantomWings;
import com.github.laxika.magicalvibes.cards.v.VeilOfSecrecy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tallowisp.class, TeardropKami.class, VeilOfSecrecy.class, PhantomWings.class,
        GenjuOfTheFields.class, GoblinCohort.class})
class TallowispTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers only Auras with enchant creature")
    void spiritSpellOffersEnchantCreatureAuras() {
        addCreatureReady(player1, new Tallowisp());
        prepareMainPhase();
        harness.setLibrary(player1, List.of(new PhantomWings(), new GenjuOfTheFields(), new GoblinCohort()));
        harness.setHand(player1, List.of(new TeardropKami()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName)).containsExactly("Phantom Wings");
    }

    @Test
    @DisplayName("Casting an Arcane spell lets you put the found Aura into your hand")
    void arcaneSpellPutsAuraInHand() {
        var tallowisp = addCreatureReady(player1, new Tallowisp());
        prepareMainPhase();
        harness.setLibrary(player1, List.of(new PhantomWings(), new GenjuOfTheFields()));
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, tallowisp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("Declining the trigger searches nothing")
    void decliningSearchesNothing() {
        var tallowisp = addCreatureReady(player1, new Tallowisp());
        prepareMainPhase();
        harness.setLibrary(player1, List.of(new PhantomWings()));
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, tallowisp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addCreatureReady(player1, new Tallowisp());
        prepareMainPhase();
        harness.setLibrary(player1, List.of(new PhantomWings()));
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Cohort");
    }

    @Test
    @DisplayName("A Spirit spell cast by an opponent does not trigger Tallowisp")
    void opponentSpiritSpellDoesNotTrigger() {
        addCreatureReady(player1, new Tallowisp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new PhantomWings()));
        harness.setHand(player2, List.of(new TeardropKami()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Teardrop Kami");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
