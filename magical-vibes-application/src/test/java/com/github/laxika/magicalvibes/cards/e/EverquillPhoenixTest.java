package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarcryPhoenix;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverquillPhoenix.class, WarcryPhoenix.class, GrizzlyBears.class})
class EverquillPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates a red Feather artifact token")
    void mutatingCreatesFeatherToken() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());

        triggerMutation(phoenix);

        Permanent feather = findPermanents(player1, "Feather").getFirst();
        assertThat(feather.getCard().isToken()).isTrue();
        assertThat(feather.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(feather.getCard().getColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Sacrificing a Feather returns a target Phoenix tapped")
    void featherReturnsTargetPhoenixTapped() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        WarcryPhoenix phoenixCard = new WarcryPhoenix();
        harness.setGraveyard(player1, List.of(phoenixCard));
        triggerMutation(phoenix);
        Permanent feather = findPermanents(player1, "Feather").getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, phoenixCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Feather")).isEmpty();
        Permanent returned = findPermanents(player1, "Warcry Phoenix").getFirst();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Feather cannot target a non-Phoenix card")
    void featherCannotTargetNonPhoenixCard() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        triggerMutation(phoenix);
        Permanent feather = findPermanents(player1, "Feather").getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void triggerMutation(Permanent phoenix) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, phoenix, List.of(phoenix.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
