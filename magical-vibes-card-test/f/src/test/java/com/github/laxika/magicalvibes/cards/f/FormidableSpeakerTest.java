package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FormidableSpeaker.class, Island.class})
class FormidableSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may discard a card and offers creature cards")
    void acceptingEtbMayDiscardAndSearchesForCreature() {
        FormidableSpeaker discard = new FormidableSpeaker();
        FormidableSpeaker creatureToFind = new FormidableSpeaker();
        harness.setHand(player1, List.of(new FormidableSpeaker(), discard));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(creatureToFind, new Island()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.stack).isEmpty();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .hasSize(1)
                .allMatch(card -> card.hasType(CardType.CREATURE));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureToFind);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creatureToFind);
    }

    @Test
    @DisplayName("Declining the ETB may does not discard or search")
    void decliningEtbMayDoesNothing() {
        FormidableSpeaker cardInHand = new FormidableSpeaker();
        harness.setHand(player1, List.of(new FormidableSpeaker(), cardInHand));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactly(cardInHand);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activated ability untaps another target permanent")
    void activatedAbilityUntapsAnotherPermanent() {
        Permanent speaker = addReadySpeaker();
        Permanent target = addTappedPermanent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(speaker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot target Formidable Speaker itself")
    void activatedAbilityCannotTargetItself() {
        Permanent speaker = addReadySpeaker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, speaker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySpeaker() {
        return addCreatureReady(player1, new FormidableSpeaker());
    }

    @Test
    void emptyHandCannotPayForSearch() {
        FormidableSpeaker creatureToFind = new FormidableSpeaker();
        harness.setLibrary(player1, List.of(creatureToFind));
        harness.castFromHand(player1, new FormidableSpeaker(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureToFind);
    }

    @Test
    void canDiscardNoncreatureAndFailToFindCreature() {
        Island discard = new Island();
        FormidableSpeaker creatureToFind = new FormidableSpeaker();
        harness.setHand(player1, List.of(new FormidableSpeaker(), discard));
        harness.setLibrary(player1, List.of(creatureToFind));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureToFind);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canUntapOpponentsLand() {
        Permanent speaker = addReadySpeaker();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(speaker.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetAnUntappedPermanent() {
        Permanent speaker = addReadySpeaker();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(speaker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new FormidableSpeaker());
        Permanent target = addTappedPermanent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresMana() {
        Permanent speaker = addReadySpeaker();
        Permanent target = addTappedPermanent();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(speaker.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardIsStillPaidWhenLibraryHasNoCreature() {
        Island discard = new Island();
        Island landInLibrary = new Island();
        harness.setHand(player1, List.of(new FormidableSpeaker(), discard));
        harness.setLibrary(player1, List.of(landInLibrary));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(landInLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSpeakerCannotActivateAgain() {
        Permanent speaker = addReadySpeaker();
        speaker.tap();
        Permanent target = addTappedPermanent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FormidableSpeaker());
        target.tap();
        return target;
    }
}
