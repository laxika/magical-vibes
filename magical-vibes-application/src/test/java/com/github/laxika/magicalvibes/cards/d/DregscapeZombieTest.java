package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DregscapeZombie.class, ResoundingThunder.class})
@DisplayName("Dregscape Zombie")
class DregscapeZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Dregscape Zombie to the battlefield with haste")
    void unearthReturnsWithHaste() {
        DregscapeZombie card = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Dregscape Zombie");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Unearthed Dregscape Zombie is exiled at the next end step")
    void unearthExiledAtEndStep() {
        DregscapeZombie card = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dregscape Zombie"));
    }

    @Test
    @DisplayName("Lethal damage exiles an unearthed Zombie instead of putting it in the graveyard")
    void lethalDamageExilesUnearthedZombie() {
        DregscapeZombie card = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Dregscape Zombie");
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
        harness.assertNotInGraveyard(player1, "Dregscape Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new DregscapeZombie()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Unearth cannot be activated while another ability is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new DregscapeZombie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Unearth cannot be activated during the opponent's turn")
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new DregscapeZombie()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Unearth requires payment of its black mana cost")
    void unearthRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new DregscapeZombie()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
    }
}
