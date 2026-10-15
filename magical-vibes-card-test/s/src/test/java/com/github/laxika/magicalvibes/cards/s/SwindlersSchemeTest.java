package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
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

@CardUsed({SwindlersScheme.class, GrizzlyBears.class, CounselOfTheSoratami.class,
        Cancel.class, RuleOfLaw.class})
class SwindlersSchemeTest extends BaseCardTest {

    @Test
    @DisplayName("A matching revealed creature counters the spell and may be cast by its opponent")
    void matchingCardTypeCountersSpellAndOffersFreeCast() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(revealed.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A revealed card with no shared card type does not counter the spell")
    void nonmatchingCardTypeDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(revealed));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, spell, "{2}{U}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getId)
                .isEqualTo(revealed.getId());
    }

    @Test
    @DisplayName("Declining the trigger leaves the opponent's spell to resolve")
    void decliningDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getId)
                .isEqualTo(revealed.getId());
    }

    private void castOpponentCreature(GrizzlyBears spell) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, spell, "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    void controllersOwnSpellDoesNotTriggerScheme() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, spell, "{1}{G}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    void decliningFreeCastStillCountersOriginalSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    void emptyLibraryDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        harness.setLibrary(player1, List.of());
        GrizzlyBears spell = new GrizzlyBears();

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void spellAlreadyCounteredStillAllowsCastingMatchingRevealedCard() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(revealed.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void freeCastCannotBypassRuleOfLaw() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        harness.addToBattlefield(player1, new RuleOfLaw());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player2, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
    }
}
