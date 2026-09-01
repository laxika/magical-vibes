package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrimclawBats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MudflatVillage.class, GrimclawBats.class, LagacLizard.class, RuinRat.class,
        SquirrelMob.class, GrizzlyBears.class, Opt.class})
class MudflatVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsBlackManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLACK))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyBlackManaCannotCastNoncreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void returnsTargetedKindredCardFromGraveyardToHandAndSacrificesItself() {
        addVillage();
        Card bat = new GrimclawBats();
        harness.setGraveyard(player1, List.of(bat));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, null, bat.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Mudflat Village");
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bat.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bat.getId()));
    }

    @Test
    void cannotTargetNonKindredCardInGraveyard() {
        addVillage();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, bear.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new MudflatVillage());
    }
}
