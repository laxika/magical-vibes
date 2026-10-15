package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XhoCaiFlickeringTalon.class, Divination.class, GrizzlyBears.class, Shock.class})
class XhoCaiFlickeringTalonTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability reduces the next noncreature spell by {1}")
    void reducesNextNoncreatureSpell() {
        harness.enterBattlefieldAndReturn(player1, new XhoCaiFlickeringTalon());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("The ETB reduction does not apply to creature spells")
    void doesNotReduceCreatureSpell() {
        harness.enterBattlefieldAndReturn(player1, new XhoCaiFlickeringTalon());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second spell each turn flickers up to one creature you control")
    void secondSpellFlickersTargetCreature() {
        harness.enterBattlefieldAndReturn(player1, new XhoCaiFlickeringTalon());
        harness.passBothPriorities();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("The flurry ability can resolve without a target")
    void flurryCanChooseNoTarget() {
        harness.enterBattlefieldAndReturn(player1, new XhoCaiFlickeringTalon());
        harness.passBothPriorities();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("The flurry ability cannot target an opponent's creature")
    void flurryCannotTargetOpponentCreature() {
        harness.enterBattlefieldAndReturn(player1, new XhoCaiFlickeringTalon());
        harness.passBothPriorities();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
