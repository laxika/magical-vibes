package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HotshotMechanic;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MechHangar.class, HotshotMechanic.class, SmugglersCopter.class, GrizzlyBears.class})
class MechHangarTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent hangar = addHangar(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hangar.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability adds mana that can cast a Pilot spell")
    void restrictedManaCastsPilotSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new HotshotMechanic()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The second ability adds mana that can cast a Vehicle spell")
    void restrictedManaCastsVehicleSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new SmugglersCopter()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The second ability's mana cannot cast another spell")
    void restrictedManaCannotCastOtherSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability animates a target Vehicle until end of turn")
    void animatesTargetVehicleUntilEndOfTurn() {
        addHangar(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SmugglersCopter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("The third ability cannot target a non-Vehicle")
    void cannotTargetNonVehicle() {
        addHangar(player1);
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHangar(Player player) {
        Permanent hangar = harness.addToBattlefieldAndReturn(player, new MechHangar());
        hangar.setSummoningSick(false);
        return hangar;
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
