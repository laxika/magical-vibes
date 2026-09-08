package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariGuildmage.class, GrizzlyBears.class, HillGiant.class, LeoninScimitar.class})
class GolgariGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature returns a target creature card from the graveyard to hand")
    void sacrificesCreatureAndReturnsTargetCreature() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Card returned = new HillGiant();
        harness.setGraveyard(player1, List.of(returned));
        addBlackActivationMana();

        harness.activateAbility(player1, 0, 0, null, returned.getId(), Zone.GRAVEYARD);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Golgari Guildmage");
    }

    @Test
    @DisplayName("The first ability can sacrifice Golgari Guildmage itself")
    void canSacrificeItself() {
        addCreatureReady(player1, new GolgariGuildmage());
        Card returned = new HillGiant();
        harness.setGraveyard(player1, List.of(returned));
        addBlackActivationMana();

        harness.activateAbility(player1, 0, 0, null, returned.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Golgari Guildmage");
    }

    @Test
    @DisplayName("The second ability puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addGreenActivationMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        addGreenActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlackActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addGreenActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
