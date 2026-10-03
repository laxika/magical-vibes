package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.b.BayouGroff;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.l.LashOfMalice;
import com.github.laxika.magicalvibes.cards.z.ZephyrBoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamStrix.class, EnvironmentalSciences.class, EagerFirstYear.class,
        LashOfMalice.class, BayouGroff.class, ZephyrBoots.class})
class DreamStrixTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when targeted by a spell and searches for a Lesson")
    void sacrificesAndLearnsWhenTargetedBySpell() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        harness.setHand(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castLashAt(strix);
        resolveSacrificeAndLearn();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(strix.getId()));
        harness.assertInGraveyard(player1, "Dream Strix");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(lesson);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Learn discards and draws when its controller has a card in hand")
    void learnsByDiscardingAndDrawing() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        Card discarded = new EagerFirstYear();
        Card drawn = new EnvironmentalSciences();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        castLashAt(strix);
        resolveSacrificeAndLearn();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("An equip ability targeting Dream Strix does not trigger its sacrifice")
    void abilityTargetDoesNotCauseSacrifice() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new ZephyrBoots());
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, strix.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dream Strix");
        assertThat(boots.getAttachedTo()).isEqualTo(strix.getId());
        harness.assertNotInGraveyard(player1, "Dream Strix");
    }

    @Test
    @DisplayName("Its controller's spell also triggers sacrifice before that spell resolves")
    void friendlySpellTriggersSacrifice() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        harness.setHand(player1, List.of(new LashOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, strix.getId());
        harness.assertOnBattlefield(player1, "Dream Strix");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dream Strix");
        harness.assertInGraveyard(player1, "Dream Strix");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lash of Malice");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Learn may decline both discarding and taking an available Lesson")
    void mayDeclineBothLearnOptions() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        Card kept = new EagerFirstYear();
        Card lesson = new EnvironmentalSciences();
        Card undrawn = new EnvironmentalSciences();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castLashAt(strix);
        resolveSacrificeAndLearn();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Dream Strix as a casting cost triggers learn without being targeted")
    void learnsWhenSacrificedAsCastingCost() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new DreamStrix());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithSacrifice(player1, 0, strix.getId());
        harness.assertInGraveyard(player1, "Dream Strix");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        harness.assertNotOnBattlefield(player1, "Bayou Groff");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bayou Groff");
    }

    private void castLashAt(Permanent target) {
        harness.setHand(player2, List.of(new LashOfMalice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, target.getId());
    }

    private void resolveSacrificeAndLearn() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
