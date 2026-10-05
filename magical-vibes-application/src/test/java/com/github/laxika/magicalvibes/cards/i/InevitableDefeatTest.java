package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InevitableDefeat.class, Cancel.class, Forest.class, GrizzlyBears.class, PlatinumEmperion.class})
class InevitableDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent, its controller loses 3 life, and you gain 3 life")
    void exilesPermanentAndExchangesLife() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InevitableDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new InevitableDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        InevitableDefeat defeat = new InevitableDefeat();
        harness.setHand(player1, List.of(defeat));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, defeat.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Exiles Platinum Emperion before its controller loses life")
    void exilesLifeTotalRestrictionBeforeLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new InevitableDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Platinum Emperion");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Platinum Emperion"));
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can exile your own permanent, losing and then gaining 3 life")
    void canTargetOwnPermanent() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InevitableDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 3);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Inevitable Defeat");
    }

    @Test
    @DisplayName("Does not exchange life when its only target has already been exiled")
    void illegalTargetPreventsAllEffectsDespiteBeingUncounterable() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InevitableDefeat()));
        harness.setHand(player2, List.of(new InevitableDefeat()));
        for (var player : List.of(player1, player2)) {
            harness.addMana(player, ManaColor.RED, 1);
            harness.addMana(player, ManaColor.WHITE, 1);
            harness.addMana(player, ManaColor.BLACK, 1);
            harness.addMana(player, ManaColor.COLORLESS, 1);
            harness.setLife(player, 20);
        }

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Inevitable Defeat");
        harness.assertInGraveyard(player2, "Inevitable Defeat");
    }
}
