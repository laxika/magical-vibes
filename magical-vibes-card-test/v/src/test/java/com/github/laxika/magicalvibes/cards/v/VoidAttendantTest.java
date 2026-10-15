package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidAttendant.class, ScourFromExistence.class})
class VoidAttendantTest extends BaseCardTest {

    @Test
    void processesOpponentOwnedExiledCardAndCreatesEldraziScion() {
        addReadyAttendant();
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(scion.getEffectivePower()).isEqualTo(1);
        assertThat(scion.getEffectiveToughness()).isEqualTo(1);
        assertThat(scion.getCard().getColors()).isEmpty();
    }

    @Test
    void eldraziScionCanBeSacrificedForColorlessMana() {
        addReadyAttendant();
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOwnExiledCard() {
        addReadyAttendant();
        ScourFromExistence ownCard = new ScourFromExistence();
        harness.setExile(player1, List.of(ownCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void controllerChoosesWhichOpponentOwnedCardToProcess() {
        addReadyAttendant();
        ScourFromExistence first = new ScourFromExistence();
        ScourFromExistence second = new ScourFromExistence();
        harness.setExile(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new VoidAttendant());
        attendant.setSummoningSick(true);
        attendant.tap();
        harness.setExile(player2, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(attendant.isTapped()).isTrue();
    }

    @Test
    void createsTokenAfterSourceIsExiledInResponse() {
        Permanent attendant = addReadyAttendant();
        harness.setExile(player2, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, attendant.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Void Attendant");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    @Test
    void processingChoiceDoesNotRevealOpponentFaceDownExiledCards() {
        addReadyAttendant();
        gd.addToExile(player2.getId(), new ScourFromExistence(), null, true);
        gd.addToExile(player2.getId(), new ScourFromExistence(), null, true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getConn1().getMessagesContaining("INTERACTION_PROMPT"))
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message).doesNotContain("Scour from Existence"));
    }

    private Permanent addReadyAttendant() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new VoidAttendant());
        attendant.setSummoningSick(false);
        return attendant;
    }
}
