package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApostleOfPurifyingLight.class, Cancel.class, GrizzlyBears.class, Murder.class, Shock.class})
class ApostleOfPurifyingLightTest extends BaseCardTest {

    @Test
    void exilesTargetCardFromOpponentsGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new GrizzlyBears();
        Card remaining = new Cancel();
        harness.setGraveyard(player2, List.of(target, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void exilesTargetCardFromOwnGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cancel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void rejectsTargetNotInGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new Cancel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Permanent apostle = addApostle(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    @Test
    void opponentsBlackSpellCannotTargetApostle() {
        Permanent apostle = addApostle(player1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, apostle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        harness.assertOnBattlefield(player1, "Apostle of Purifying Light");
    }

    @Test
    void controllersBlackSpellCannotTargetApostle() {
        Permanent apostle = addApostle(player1);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, apostle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void nonBlackSpellCanDamageApostle() {
        Permanent apostle = addApostle(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, apostle.getId());

        harness.assertNotOnBattlefield(player2, "Apostle of Purifying Light");
        harness.assertInGraveyard(player2, "Apostle of Purifying Light");
    }

    @Test
    void tappedSummoningSickApostleCanActivateRepeatedly() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfPurifyingLight());
        apostle.setSummoningSick(true);
        apostle.tap();
        Card first = new Murder();
        Card second = new Shock();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);

        harness.activateAbility(player1, apostleIndex, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, apostleIndex, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(first, second);
        assertThat(apostle.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent apostle = addApostle(player1);
        Card target = new Murder();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addApostle(Player player) {
        Permanent apostle = harness.addToBattlefieldAndReturn(player, new ApostleOfPurifyingLight());
        apostle.setSummoningSick(false);
        return apostle;
    }
}
