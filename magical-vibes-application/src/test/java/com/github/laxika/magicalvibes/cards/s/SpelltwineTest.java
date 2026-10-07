package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CleaverRiot;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spelltwine.class, Divination.class, SearingSpear.class, WalkingCorpse.class, CleaverRiot.class,
        Negate.class, SecretsOfTheDead.class})
class SpelltwineTest extends BaseCardTest {

    private void addSpelltwineMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Casts a copy of a card from each graveyard and exiles both originals")
    void castsCopiesOfBothTargets() {
        Divination ownCounsel = new Divination();
        Divination opponentCounsel = new Divination();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveSorcery(player1, 0, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities(); // resolve the first copy
        harness.passBothPriorities(); // resolve the second copy

        // Spelltwine cast the Spelltwine card out of hand, so both copies drew two cards each.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 4);

        // Both originals were exiled, and neither graveyard got a copy put into it.
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(exiledNames(player1)).contains("Divination", "Spelltwine");
        assertThat(exiledNames(player2)).contains("Divination");
    }

    @Test
    @DisplayName("A copied card that needs a target prompts for one")
    void copyOfTargetedCardPromptsForTarget() {
        Divination ownCounsel = new Divination();
        SearingSpear opponentSearingSpear = new SearingSpear();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentSearingSpear));
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse()).getId();

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        harness.castAndResolveSorcery(player1, 0, List.of(ownCounsel.getId(), opponentSearingSpear.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Both targets may not come from the same graveyard")
    void rejectsTwoTargetsFromOwnGraveyard() {
        Divination first = new Divination();
        Divination second = new Divination();
        harness.setGraveyard(player1, List.of(first, second));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("A creature card in a graveyard is not a legal target")
    void rejectsCreatureCardTarget() {
        Divination ownCounsel = new Divination();
        WalkingCorpse opponentBears = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentBears));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(ownCounsel.getId(), opponentBears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves its graveyard before resolution is simply skipped")
    void skipsTargetThatLeftGraveyard() {
        Divination ownCounsel = new Divination();
        Divination opponentCounsel = new Divination();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities(); // Only the surviving copy is cast.
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    @Test
    @DisplayName("The controller chooses the casting order of two different copies")
    void offersChoiceOfCopyCastingOrder() {
        Divination ownDivination = new Divination();
        CleaverRiot opponentRiot = new CleaverRiot();
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of(opponentRiot));
        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        harness.castAndResolveSorcery(player1, 0,
                List.of(ownDivination.getId(), opponentRiot.getId()));

        assertThat(gd.interaction.isAwaitingInput())
                .as("Neither copy requires targets or modes, but their casting order must be chosen")
                .isTrue();
    }

    @Test
    @DisplayName("A copied Negate can target the still-resolving Spelltwine")
    void counterspellCopyCanTargetResolvingSpelltwine() {
        Negate ownNegate = new Negate();
        Divination opponentDivination = new Divination();
        Spelltwine spelltwine = new Spelltwine();
        harness.setGraveyard(player1, List.of(ownNegate));
        harness.setGraveyard(player2, List.of(opponentDivination));
        harness.setHand(player1, List.of(spelltwine));
        addSpelltwineMana();

        harness.castSorcery(player1, 0,
                List.of(ownNegate.getId(), opponentDivination.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput())
                .as("Spelltwine remains a legal noncreature spell target while casting the Negate copy")
                .isTrue();
        harness.handlePermanentChosen(player1, spelltwine.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(exiledNames(player1)).containsExactlyInAnyOrder("Negate", "Spelltwine");
        harness.assertNotInGraveyard(player1, "Negate");
    }

    @Test
    @DisplayName("Spelltwine does not resolve or exile itself when both targets become illegal")
    void doesNotResolveWhenBothTargetsLeaveGraveyards() {
        Divination ownDivination = new Divination();
        Divination opponentDivination = new Divination();
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of(opponentDivination));
        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        harness.castSorcery(player1, 0,
                List.of(ownDivination.getId(), opponentDivination.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spelltwine");
        assertThat(exiledNames(player1)).isEmpty();
        assertThat(exiledNames(player2)).isEmpty();
    }

    @Test
    @DisplayName("Spelltwine cannot be cast with only one graveyard target")
    void requiresBothTargetsToCast() {
        Divination ownDivination = new Divination();
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownDivination.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Spelltwine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Spelltwine.class, Divination.class, SecretsOfTheDead.class})
    @DisplayName("Copies are cast from exile and do not trigger Secrets of the Dead")
    void copiesDoNotTriggerCastingFromGraveyard() {
        Divination ownDivination = new Divination();
        Divination opponentDivination = new Divination();
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of(opponentDivination));
        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        harness.castAndResolveSorcery(player1, 0,
                List.of(ownDivination.getId(), opponentDivination.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private List<String> exiledNames(com.github.laxika.magicalvibes.model.Player player) {
        return gd.getPlayerExiledCards(player.getId()).stream().map(Card::getName).toList();
    }
}
