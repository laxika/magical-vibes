package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LlanowarCavalry;
import com.github.laxika.magicalvibes.cards.f.FactOrFiction;
import com.github.laxika.magicalvibes.cards.s.Shackles;
import com.github.laxika.magicalvibes.cards.s.Skizzik;
import com.github.laxika.magicalvibes.cards.t.TolarianEmissary;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptAngel.class, TolarianEmissary.class, Skizzik.class, LlanowarCavalry.class,
        Cremate.class, FactOrFiction.class, Shackles.class})
class CryptAngelTest extends BaseCardTest {

    private void castCryptAngel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CryptAngel(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a blue creature card from the graveyard to hand")
    void etbReturnsBlueCreatureToHand() {
        TolarianEmissary blueCreature = new TolarianEmissary();
        harness.setGraveyard(player1, List.of(blueCreature));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(blueCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tolarian Emissary");
        harness.assertNotInGraveyard(player1, "Tolarian Emissary");
    }

    @Test
    @DisplayName("ETB returns a red creature card from the graveyard to hand")
    void etbReturnsRedCreatureToHand() {
        Skizzik redCreature = new Skizzik();
        harness.setGraveyard(player1, List.of(redCreature));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(redCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skizzik");
        harness.assertNotInGraveyard(player1, "Skizzik");
    }

    @Test
    @DisplayName("A green creature card is not a legal target")
    void greenCreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new LlanowarCavalry()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Llanowar Cavalry");
    }

    @Test
    @DisplayName("A creature card in an opponent's graveyard is not a legal target")
    void opponentCreatureIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new TolarianEmissary()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Tolarian Emissary");
    }

    @Test
    @DisplayName("A noncreature card is not a legal target")
    void noncreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new Cremate()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Cremate");
    }

    @Test
    @DisplayName("A blue noncreature card is not a legal target")
    void blueNoncreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new FactOrFiction()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Fact or Fiction");
    }

    @Test
    @DisplayName("Only the selected creature is returned when multiple creatures qualify")
    void returnsOnlySelectedCreature() {
        TolarianEmissary blueCreature = new TolarianEmissary();
        Skizzik redCreature = new Skizzik();
        harness.setGraveyard(player1, List.of(blueCreature, redCreature));

        castCryptAngel();
        harness.handleMultipleCardsChosen(player1, List.of(redCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skizzik");
        harness.assertNotInGraveyard(player1, "Skizzik");
        harness.assertInGraveyard(player1, "Tolarian Emissary");
        harness.assertNotInHand(player1, "Tolarian Emissary");
    }

    @Test
    @DisplayName("An exiled target is not returned and no replacement target is chosen")
    void targetExiledInResponseIsNotReturned() {
        TolarianEmissary target = new TolarianEmissary();
        Skizzik otherCreature = new Skizzik();
        harness.setGraveyard(player1, List.of(target, otherCreature));

        castCryptAngel();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player2, List.of(new Cremate()));
        harness.setLibrary(player2, List.of(new LlanowarCavalry(), new TolarianEmissary()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertNotInHand(player1, "Tolarian Emissary");
        harness.assertInGraveyard(player1, "Skizzik");
        harness.assertNotInHand(player1, "Skizzik");
    }

    @Test
    @DisplayName("Protection from white prevents a white Aura from targeting Crypt Angel")
    void whiteAuraCannotTargetCryptAngel() {
        harness.addToBattlefield(player2, new CryptAngel());
        harness.setHand(player1, List.of(new Shackles()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                harness.getPermanentId(player2, "Crypt Angel")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Crypt Angel");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Crypt Angel")
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new CryptAngel());
        addCreatureReady(player2, new LlanowarCavalry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blue flying creature can block Crypt Angel")
    void blueFlyingCreatureCanBlock() {
        addCreatureReady(player1, new CryptAngel());
        var blocker = addCreatureReady(player2, new TolarianEmissary());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
