package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeathsDuet;
import com.github.laxika.magicalvibes.cards.n.Nausea;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Anarchist.class, DeathsDuet.class, Nausea.class, RagingGoblin.class})
class AnarchistTest extends BaseCardTest {

    /**
     * Casts Anarchist and resolves its creature spell, leaving its ETB target choice pending.
     */
    private void castAnarchist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Anarchist(), "{4}{R}");
        harness.passBothPriorities();
    }

    /** Chooses the targeted sorcery and resolves the ETB ability to its may prompt. */
    private void castAndChooseTarget(Card target) {
        castAnarchist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    /** Casts Anarchist, chooses its target, and accepts the may ability. */
    private void castAndAcceptMay(Card target) {
        castAndChooseTarget(target);
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Chooses the graveyard target before resolving Anarchist's may ability")
    void choosesTargetBeforeMayPrompt() {
        Card sorcery = new DeathsDuet();
        harness.setGraveyard(player1, List.of(sorcery));

        castAnarchist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the may ability leaves the targeted sorcery in the graveyard")
    void decliningMaySkipsAbility() {
        Card sorcery = new DeathsDuet();
        harness.setGraveyard(player1, List.of(sorcery));

        castAndChooseTarget(sorcery);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Death's Duet");
    }

    @Test
    @DisplayName("Returns the targeted sorcery card from the graveyard to hand")
    void returnsSorceryFromGraveyardToHand() {
        Card sorcery = new DeathsDuet();
        harness.setGraveyard(player1, List.of(sorcery));

        castAndAcceptMay(sorcery);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Death's Duet");
        harness.assertNotInGraveyard(player1, "Death's Duet");
    }

    @Test
    @DisplayName("Chooses a specific sorcery when multiple are in the graveyard")
    void choosesSpecificSorcery() {
        Card firstSorcery = new DeathsDuet();
        Card selectedSorcery = new Nausea();
        harness.setGraveyard(player1, List.of(firstSorcery, selectedSorcery));

        castAnarchist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstSorcery.getId(), selectedSorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(selectedSorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Nausea");
        harness.assertInGraveyard(player1, "Death's Duet");
        harness.assertNotInGraveyard(player1, "Nausea");
    }

    @Test
    @DisplayName("Does not offer a non-sorcery card as a graveyard target")
    void cannotReturnNonSorcery() {
        Card creature = new RagingGoblin();
        Card sorcery = new DeathsDuet();
        harness.setGraveyard(player1, List.of(creature, sorcery));

        castAnarchist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Death's Duet");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not put the ability on the stack when its controller has no sorcery target")
    void noEffectWithNoSorceriesInGraveyard() {
        harness.setGraveyard(player1, List.of(new RagingGoblin()));

        castAnarchist();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not target a sorcery in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        harness.setGraveyard(player2, List.of(new Nausea()));

        castAnarchist();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Nausea");
    }
}
