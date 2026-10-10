package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SecretsOfTheGoldenCity;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireFleetDaredevil.class, SecretsOfTheGoldenCity.class, RaptorCompanion.class,
        Island.class, Negate.class})
class DireFleetDaredevilTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only an opponent's instant or sorcery card")
    void etbTargetsOpponentInstantOrSorcery() {
        SecretsOfTheGoldenCity ownSpell = new SecretsOfTheGoldenCity();
        SecretsOfTheGoldenCity opponentSpell = new SecretsOfTheGoldenCity();
        RaptorCompanion opponentCreature = new RaptorCompanion();
        harness.setGraveyard(player1, List.of(ownSpell));
        harness.setGraveyard(player2, List.of(opponentSpell, opponentCreature));

        castDaredevil();

        PendingInteraction.MultiGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentSpell.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentSpell.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(opponentSpell.getId())).isNotNull();
        assertThat(harness.getGameData().findExiledCard(ownSpell.getId())).isNull();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("ETB grants this-turn casting with any mana and exiles the spell afterward")
    void etbSpellCanBeCastWithAnyManaAndIsExiledAfterward() {
        SecretsOfTheGoldenCity spell = new SecretsOfTheGoldenCity();
        harness.setGraveyard(player2, List.of(spell));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castDaredevil();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().exilePlayAnyManaType).contains(spell.getId());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(spell.getId())).isNotNull();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("ETB does not prompt when opponents have no instant or sorcery cards")
    void etbHasNoTargetForNonSpellCards() {
        harness.setGraveyard(player2, List.of(new RaptorCompanion()));

        castDaredevil();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
    }

    private void castDaredevil() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DireFleetDaredevil(), "{1}{R}");
        harness.passBothPriorities();
    }

    @Test
    void sorceryCannotBeCastOutsideMainPhase() {
        SecretsOfTheGoldenCity spell = exileSorcery();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        SecretsOfTheGoldenCity spell = exileSorcery();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void opponentCannotUseCastingPermission() {
        SecretsOfTheGoldenCity spell = exileSorcery();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void resolvedSpellCannotBeCastAgain() {
        SecretsOfTheGoldenCity spell = exileSorcery();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counteredSpellIsExiledInOwnersZone() {
        SecretsOfTheGoldenCity spell = exileSorcery();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, spell.getId());
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.findExiledCard(spell.getId()).ownerId()).isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void instantCanBeCastInResponseToSpell() {
        Negate spell = new Negate();
        harness.setGraveyard(player2, List.of(spell));
        castDaredevil();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        SecretsOfTheGoldenCity otherSpell = new SecretsOfTheGoldenCity();
        harness.castFromHand(player1, otherSpell, "{1}{U}{U}");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId(), otherSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void abilityDoesNotExileTargetThatLeftGraveyard() {
        SecretsOfTheGoldenCity spell = new SecretsOfTheGoldenCity();
        harness.setGraveyard(player2, List.of(spell));
        castDaredevil();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(spell));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(spell);
    }

    private SecretsOfTheGoldenCity exileSorcery() {
        SecretsOfTheGoldenCity spell = new SecretsOfTheGoldenCity();
        harness.setGraveyard(player2, List.of(spell));
        castDaredevil();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        return spell;
    }
}
