package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChameleonMasterOfDisguise.class, GrizzlyBears.class})
class ChameleonMasterOfDisguiseTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature its controller controls while retaining its own name")
    void copiesOwnCreatureAndRetainsName() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFromHand();

        chooseCopy(harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent chameleon = findChameleon();
        assertThat(chameleon.getCard().getName()).isEqualTo("Chameleon, Master of Disguise");
        assertThat(chameleon.getCard().getPower()).isEqualTo(2);
        assertThat(chameleon.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot copy a creature controlled by an opponent")
    void cannotCopyOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFromHand();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentCreatureId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        assertThat(choice.validPermanentIds()).contains(ownCreatureId).doesNotContain(opponentCreatureId);
    }

    @Test
    @DisplayName("Mayhem casts the card from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        ChameleonMasterOfDisguise chameleon = new ChameleonMasterOfDisguise();
        harness.setGraveyard(player1, List.of(chameleon));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(chameleon.getId())));
        prepareMainPhase();
        addMayhemMana();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findChameleon()).isNotNull();
    }

    @Test
    @DisplayName("Mayhem cannot cast the card from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        ChameleonMasterOfDisguise chameleon = new ChameleonMasterOfDisguise();
        harness.setGraveyard(player1, List.of(chameleon));
        prepareMainPhase();
        addMayhemMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new ChameleonMasterOfDisguise()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void chooseCopy(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addMayhemMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent findChameleon() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName()
                        .equals("Chameleon, Master of Disguise"))
                .findFirst()
                .orElseThrow();
    }
}
