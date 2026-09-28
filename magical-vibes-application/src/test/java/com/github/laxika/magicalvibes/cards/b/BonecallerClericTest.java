package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonecallerCleric.class, GrizzlyBears.class, HolyDay.class})
class BonecallerClericTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the targeted creature from the graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        addCreatureReady(player1, new BonecallerCleric());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        addManaForAbility();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Bonecaller Cleric");
        harness.assertInGraveyard(player1, "Bonecaller Cleric");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pays the mana and sacrifices Bonecaller Cleric as a cost")
    void paysManaAndSacrificesSelf() {
        addCreatureReady(player1, new BonecallerCleric());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Bonecaller Cleric");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addCreatureReady(player1, new BonecallerCleric());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        addManaForAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bonecaller Cleric");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        addCreatureReady(player1, new BonecallerCleric());
        HolyDay target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        addManaForAbility();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Bonecaller Cleric");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
