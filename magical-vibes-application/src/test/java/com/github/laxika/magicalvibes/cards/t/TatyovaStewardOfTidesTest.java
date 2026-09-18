package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatyovaStewardOfTides.class, Forest.class})
class TatyovaStewardOfTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall animates up to one controlled land once seven lands are controlled")
    void landfallAnimatesControlledLandAtSevenLands() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId(), player1.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Landfall does not trigger before controlling seven lands")
    void landfallDoesNotTriggerBelowSevenLands() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Landfall only permits controlled lands and may be declined")
    void landfallRestrictsTargetsAndMayBeDeclined() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent ownLand = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gqs.isCreature(gd, ownLand)).isFalse();
    }
}
