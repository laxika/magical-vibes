package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Helbrute.class, GrizzlyBears.class, Shock.class})
class HelbruteTest extends BaseCardTest {

    @Test
    void canCastFromGraveyardByExilingAnotherCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Helbrute(), bears));
        addHelbruteMana();

        harness.castFromGraveyard(player1, 0, List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Helbrute");
    }

    @Test
    void cannotCastFromGraveyardWithoutAnotherCreature() {
        harness.setGraveyard(player1, List.of(new Helbrute()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Helbrute");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    void cannotExileNoncreatureCardForGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Helbrute(), new Shock()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Helbrute");
    }

    @Test
    void cannotExileItselfEvenWhenAnotherCreatureIsAvailable() {
        Helbrute helbrute = new Helbrute();
        Helbrute other = new Helbrute();
        harness.setGraveyard(player1, List.of(helbrute, other));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(helbrute, other);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    void canExileAnotherHelbruteBeforeItsGraveyardIndex() {
        Helbrute payment = new Helbrute();
        Helbrute spell = new Helbrute();
        harness.setGraveyard(player1, List.of(payment, spell));
        addHelbruteMana();

        harness.castFromGraveyard(player1, 1, List.of(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(payment);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Helbrute");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(spell));
    }

    @Test
    void cannotExileMoreThanOneCreature() {
        harness.setGraveyard(player1, List.of(new Helbrute(), new Helbrute(), new Helbrute()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastWithoutPayingTheManaCost() {
        harness.setGraveyard(player1, List.of(new Helbrute(), new Helbrute()));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseAnOpponentsCreatureToPayTheCost() {
        harness.setGraveyard(player1, List.of(new Helbrute()));
        harness.setGraveyard(player2, List.of(new Helbrute()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Helbrute");
        harness.assertInGraveyard(player2, "Helbrute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void castingFromHandDoesNotRequireExilingACreature() {
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, new Helbrute(), "{3}{B}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Helbrute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingInResponseToASpell() {
        harness.castFromHand(player1, new Helbrute(), "{3}{B}{R}");
        harness.setGraveyard(player1, List.of(new Helbrute(), new Helbrute()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void canAttackTheTurnItIsCastFromTheGraveyard() {
        harness.setGraveyard(player1, List.of(new Helbrute(), new Helbrute()));
        addHelbruteMana();
        harness.castFromGraveyard(player1, 0, List.of(1));
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    private void addHelbruteMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
