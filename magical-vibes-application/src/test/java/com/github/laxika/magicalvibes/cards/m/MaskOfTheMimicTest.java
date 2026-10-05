package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskOfTheMimic.class, SpinedWurm.class, HonorGuard.class, PsychogenicProbe.class})
class MaskOfTheMimicTest extends BaseCardTest {
    @Test
    @DisplayName("A face-down nontoken creature is a legal target but has no matching name")
    void faceDownTargetDoesNotFindACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Card copy = new SpinedWurm();
        harness.setLibrary(player1, List.of(copy));
        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Mask of the Mimic");
    }

    @Test
    @CardUsed({PsychogenicProbe.class})
    @DisplayName("Searching an empty library still shuffles it and triggers Psychogenic Probe")
    void emptyLibraryStillTriggersShuffleAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mask of the Mimic");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the additional cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new HonorGuard());
        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spined Wurm");
        harness.assertOnBattlefield(player2, "Honor Guard");
        harness.assertInHand(player1, "Mask of the Mimic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices a creature and puts a same-named library card onto the battlefield")
    void sacrificesAndFetchesSameNamedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        harness.setLibrary(player1, List.of(new SpinedWurm(), new HonorGuard()));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Spined Wurm");

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Spined Wurm")).hasSize(2);
        assertThat(findPermanents(player1, "Spined Wurm")).noneMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searches the casting player's library when the target is controlled by an opponent")
    void searchesCastingPlayersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Card controllerCopy = new SpinedWurm();
        Card opponentCopy = new SpinedWurm();
        harness.setLibrary(player1, List.of(controllerCopy));
        harness.setLibrary(player2, List.of(opponentCopy));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).containsExactly(controllerCopy);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Spined Wurm")).hasSize(1);
        assertThat(findPermanents(player2, "Spined Wurm")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCopy);
    }

    @Test
    @DisplayName("Sacrifices the cost creature and does nothing when no same-named card is found")
    void doesNothingWhenNoSameNamedCardIsFound() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        harness.setLibrary(player1, List.of(new HonorGuard()));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Spined Wurm");
        harness.assertNotOnBattlefield(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Mask of the Mimic");
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Honor Guard");
    }

    @Test
    @DisplayName("May decline to put a matching library card onto the battlefield")
    void mayDeclineMatchingSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Card copy = new SpinedWurm();
        harness.setLibrary(player1, List.of(copy));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Spined Wurm")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not search when the targeted creature is sacrificed as the additional cost")
    void sacrificedTargetIsNoLongerLegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Card copy = new SpinedWurm();
        harness.setLibrary(player1, List.of(copy));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertNotOnBattlefield(player1, "Spined Wurm");
        harness.assertInGraveyard(player1, "Spined Wurm");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
        harness.assertInGraveyard(player1, "Mask of the Mimic");
    }

    @Test
    @DisplayName("Cannot cast without sacrificing exactly one creature")
    void cannotCastWithoutSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        harness.assertOnBattlefield(player1, "Spined Wurm");
    }

    @Test
    @DisplayName("Cannot target a token creature")
    void cannotTargetTokenCreature() {
        Card tokenCard = new Card();
        tokenCard.setName("Wurm Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(5);
        tokenCard.setToughness(4);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, token.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Wurm Token")).hasSize(1);
        assertThat(findPermanents(player1, "Honor Guard")).hasSize(1);
    }

    @Test
    @DisplayName("A same-named instant or sorcery remains in the library when chosen")
    void nonPermanentSameNamedCardDoesNotEnterBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Card sameNamedSorcery = new Card();
        sameNamedSorcery.setName("Spined Wurm");
        sameNamedSorcery.setType(CardType.SORCERY);
        harness.setLibrary(player1, List.of(sameNamedSorcery));

        harness.setHand(player1, List.of(new MaskOfTheMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sameNamedSorcery);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Spined Wurm")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sameNamedSorcery);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
