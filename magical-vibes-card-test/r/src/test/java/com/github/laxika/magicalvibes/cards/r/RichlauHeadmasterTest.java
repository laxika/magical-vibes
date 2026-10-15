package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RichlauHeadmaster.class, IchorWellspring.class, Ornithopter.class, Forest.class})
class RichlauHeadmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} perpetually reduces an artifact card's cost and puts it second from the top")
    void paysForArtifactCard() {
        IchorWellspring target = new IchorWellspring();
        Forest nonArtifact = new Forest();
        Forest topCard = new Forest();
        Forest bottomCard = new Forest();
        harness.addToBattlefield(player1, new RichlauHeadmaster());
        harness.setGraveyard(player1, List.of(nonArtifact, target));
        harness.setLibrary(player1, List.of(topCard, bottomCard));

        resolveRichlauAndPay();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nonArtifact.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, target, bottomCard);
        assertThat(gd.perpetualCardCastCostReductions).containsEntry(target.getId(), 1);
        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(target.getId());
    }

    @Test
    @DisplayName("An artifact creature also gets a perpetual +2/+2")
    void boostsArtifactCreature() {
        Ornithopter target = new Ornithopter();
        Forest topCard = new Forest();
        harness.addToBattlefield(player1, new RichlauHeadmaster());
        harness.setGraveyard(player1, List.of(new Forest(), target));
        harness.setLibrary(player1, List.of(topCard));

        resolveRichlauAndPay();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, target);
        assertThat(gd.perpetualCardCastCostReductions).containsEntry(target.getId(), 1);
        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(target.getId(), new CardPowerToughnessModifier(2, 2));
    }

    @Test
    @DisplayName("Declining the payment leaves the graveyard and library unchanged")
    void declinesPayment() {
        IchorWellspring target = new IchorWellspring();
        Forest topCard = new Forest();
        harness.addToBattlefield(player1, new RichlauHeadmaster());
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(topCard));

        resolveRichlau(false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.perpetualCardCastCostReductions).doesNotContainKey(target.getId());
    }

    private void resolveRichlauAndPay() {
        resolveRichlau(true);
    }

    private void resolveRichlau(boolean pay) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        if (pay) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }
        harness.handleMayAbilityChosen(player1, pay);
    }
}
