package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentalSynthesizer.class, Forest.class, GrizzlyBears.class})
class ExperimentalSynthesizerTest extends BaseCardTest {

    @Test
    void enteringExilesTopCardWithPlayPermission() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ExperimentalSynthesizer()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    void sacrificingItExilesTopCardAndCreatesVigilantSamurai() {
        Permanent synthesizer = harness.addToBattlefieldAndReturn(player1, new ExperimentalSynthesizer());
        synthesizer.setSummoningSick(false);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(synthesizer.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(synthesizer.getCard());

        Permanent samurai = findPermanent(player1, "Samurai");
        assertThat(samurai.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(samurai.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(samurai.getCard().getSubtypes()).containsExactly(CardSubtype.SAMURAI);
        assertThat(samurai.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    void cannotActivateOutsideSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new ExperimentalSynthesizer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, (UUID) null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
