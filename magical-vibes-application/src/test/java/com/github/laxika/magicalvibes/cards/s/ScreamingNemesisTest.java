package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingNemesis.class, GrizzlyBears.class, AngelOfMercy.class, Shock.class})
class ScreamingNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("Reflects damage to another target creature")
    void reflectsDamageToAnotherCreature() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, nemesisId);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bearsId).doesNotContain(nemesisId);

        harness.handlePermanentChosen(player2, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Locks life gain for a player actually dealt reflected damage")
    void locksLifeGainForDamagedPlayer() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        harness.castInstant(player1, 0, nemesisId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not lock life gain when the reflected damage is prevented")
    void preventedReflectedDamageDoesNotLockLifeGain() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        gd.playersWithAllDamagePrevented.add(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        harness.castInstant(player1, 0, nemesisId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }
}
