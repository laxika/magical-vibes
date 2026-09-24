package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkkiDrillmaster;
import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.f.FiddleheadKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExileIntoDarkness.class, AkkiDrillmaster.class, ArabaMothrider.class,
        FiddleheadKami.class})
class ExileIntoDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature with mana value 3 or less, including 3")
    void sacrificesEligibleCreature() {
        harness.addToBattlefield(player2, new AkkiDrillmaster());
        harness.addToBattlefield(player2, new FiddleheadKami());

        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Akki Drillmaster");
        harness.assertOnBattlefield(player2, "Fiddlehead Kami");
        harness.assertInGraveyard(player2, "Akki Drillmaster");
    }

    @Test
    @DisplayName("Target player chooses which eligible creature to sacrifice")
    void targetPlayerChoosesEligibleCreature() {
        harness.addToBattlefield(player2, new AkkiDrillmaster());
        harness.addToBattlefield(player2, new ArabaMothrider());

        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent chosen = findPermanent(player2, "Araba Mothrider");
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        harness.assertNotOnBattlefield(player2, "Araba Mothrider");
        harness.assertOnBattlefield(player2, "Akki Drillmaster");
    }

    @Test
    @DisplayName("Does nothing when the target player controls no eligible creature")
    void noEligibleCreatureNoSacrifice() {
        harness.addToBattlefield(player2, new FiddleheadKami());

        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fiddlehead Kami");
        harness.assertInGraveyard(player1, "Exile into Darkness");
    }

    @Test
    @DisplayName("Returns itself from the graveyard when the controller has more cards in hand")
    void returnsFromGraveyardWithHandAdvantage() {
        ExileIntoDarkness card = new ExileIntoDarkness();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new ExileIntoDarkness(), new ExileIntoDarkness()));
        harness.setHand(player2, List.of(new ExileIntoDarkness()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Exile into Darkness");
        harness.assertNotInGraveyard(player1, "Exile into Darkness");
    }

    @Test
    @DisplayName("Does not trigger from the graveyard without hand advantage")
    void doesNotReturnWithoutHandAdvantage() {
        harness.setGraveyard(player1, List.of(new ExileIntoDarkness()));
        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.setHand(player2, List.of(new ExileIntoDarkness()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not return from the graveyard if hand advantage is lost before resolution")
    void doesNotReturnAfterHandAdvantageIsLost() {
        ExileIntoDarkness card = new ExileIntoDarkness();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new ExileIntoDarkness(), new ExileIntoDarkness()));
        harness.setHand(player2, List.of(new ExileIntoDarkness()));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Exile into Darkness");
    }

    @Test
    @DisplayName("Declining the optional return leaves the card in the graveyard")
    void decliningReturnLeavesCardInGraveyard() {
        ExileIntoDarkness card = new ExileIntoDarkness();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new AkkiDrillmaster()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Exile into Darkness");
        harness.assertNotInHand(player1, "Exile into Darkness");
    }
}
