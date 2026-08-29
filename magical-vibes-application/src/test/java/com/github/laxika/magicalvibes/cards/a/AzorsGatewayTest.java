package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AzorsGatewayTest extends BaseCardTest {

    @Test
    void drawsThenExilesAChosenCardAndTracksItWithGateway() {
        Permanent gateway = addGateway();
        Card drawn = new Shock();
        Card exiled = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(gateway.getId())).containsExactly(exiled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gateway.isTransformed()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void transformsAfterFiveDifferentManaValuesAndBackFaceAddsCurrentLifeTotal() {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());

        Card drawn = new Shock();
        Card exiled = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gateway.isTransformed()).isTrue();
        assertThat(gateway.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.getCardsExiledByPermanent(gateway.getId())).contains(exiled);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(25);
    }

    @Test
    void doesNotTransformWhenExiledCardsHaveOnlyFourDifferentManaValues() {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());

        Card drawn = new Shock();
        Card exiled = new HillGiant();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gateway.isTransformed()).isFalse();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addGateway() {
        Permanent gateway = new Permanent(new AzorsGateway());
        gateway.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gateway);
        return gateway;
    }
}
