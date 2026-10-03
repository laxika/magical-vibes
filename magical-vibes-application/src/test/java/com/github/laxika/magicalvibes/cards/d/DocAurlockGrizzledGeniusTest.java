package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DocAurlockGrizzledGenius.class, DjinnOfFoolsFall.class, SqueeTheImmortal.class})
class DocAurlockGrizzledGeniusTest extends BaseCardTest {

    @Test
    void reducesPlotCostFromHand() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(djinn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.plottedCardIds).contains(djinn.getId());
    }

    @Test
    void reducesCastCostFromGraveyard() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void reducesCastCostFromExile() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, squee.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void doesNotReduceNormalHandCast() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        harness.setHand(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plotReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        harness.setHand(player1, List.of(new DjinnOfFoolsFall()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.plottedCardIds).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void graveyardReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Squee, the Immortal");
    }

    @Test
    void doesNotReduceOpponentsPlotCost() {
        harness.addToBattlefield(player2, new DocAurlockGrizzledGenius());
        harness.setHand(player1, List.of(new DjinnOfFoolsFall()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.plottedCardIds).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsGraveyardCast() {
        harness.addToBattlefield(player2, new DocAurlockGrizzledGenius());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsExileCast() {
        harness.addToBattlefield(player2, new DocAurlockGrizzledGenius());
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantPermissionToCastFromGraveyard() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        harness.setGraveyard(player1, List.of(new DjinnOfFoolsFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Djinn of Fool's Fall");
    }

    @Test
    void doesNotGrantPermissionToCastFromExile() {
        harness.addToBattlefield(player1, new DocAurlockGrizzledGenius());
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.setExile(player1, List.of(djinn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, djinn.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(djinn.getId())).isNotNull();
    }
}
