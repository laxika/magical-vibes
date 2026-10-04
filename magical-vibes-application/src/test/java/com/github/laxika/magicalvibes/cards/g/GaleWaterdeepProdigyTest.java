package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.r.RemoveSoul;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaleWaterdeepProdigy.class, CounselOfTheSoratami.class, Shock.class, ThinkTwice.class,
        Hurricane.class, RemoveSoul.class, Cancel.class})
class GaleWaterdeepProdigyTest extends BaseCardTest {

    @Test
    void instantFromHandMayCastOnlyASorceryFromOwnGraveyard() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(counsel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    void sorceryFromHandMayCastOnlyAnInstantFromOwnGraveyard() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void spellsCastFromGraveyardDoNotTriggerGale() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningToCastLeavesTargetInGraveyard() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void mayChooseNoGraveyardTarget() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        harness.setGraveyard(player1, List.of(new CounselOfTheSoratami()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void spellWithoutLegalTargetsRemainsInGraveyard() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        RemoveSoul removeSoul = new RemoveSoul();
        harness.setGraveyard(player1, List.of(removeSoul));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removeSoul.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Remove Soul");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void mayChooseNonzeroXForGraveyardSpell() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        Hurricane hurricane = new Hurricane();
        harness.setGraveyard(player1, List.of(hurricane));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(hurricane.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleXValueChosen(player1, 2);
        resolveAllTriggers();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hurricane.getId()));
    }

    @Test
    void graveyardTargetsExcludeWrongTypeAndOpponentsCards() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel, new Shock()));
        harness.setGraveyard(player2, List.of(new CounselOfTheSoratami()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(counsel.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();
    }

    @Test
    void mayCastCounterspellTargetingOriginalSorcery() {
        harness.addToBattlefield(player1, new GaleWaterdeepProdigy());
        Cancel cancel = new Cancel();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(cancel));
        harness.setHand(player1, List.of(counsel));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(cancel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, counsel.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cancel.getId()));
    }
}
