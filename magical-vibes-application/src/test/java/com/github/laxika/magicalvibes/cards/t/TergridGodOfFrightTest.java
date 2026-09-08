package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TergridGodOfFright.class, CruelEdict.class, Distress.class, GrizzlyBears.class, Shock.class})
class TergridGodOfFrightTest extends BaseCardTest {

    @Test
    void returnsAnOpponentsDiscardedPermanentUnderItsControl() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsAnOpponentsSacrificedNontokenPermanentUnderItsControl() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerWhenAnOpponentsPermanentIsDestroyed() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canCastAndActivateTheBackFace() {
        TergridGodOfFright card = new TergridGodOfFright();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent lantern = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of());
        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();

        assertThat(lantern.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(lantern.isTapped()).isFalse();
    }
}
