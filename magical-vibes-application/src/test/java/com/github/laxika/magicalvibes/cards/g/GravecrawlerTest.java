package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravecrawlerTest extends BaseCardTest {

    

    @Test
    @DisplayName("Can cast from graveyard while controlling a Zombie")
    void canCastFromGraveyardWithZombie() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gravecrawler");
    }

    @Test
    @DisplayName("Cannot cast from graveyard without controlling a Zombie")
    void cannotCastFromGraveyardWithoutZombie() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Opponent's Zombie does not enable casting from graveyard")
    void opponentZombieDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player2, new GravebornMuse());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Resolves onto battlefield from graveyard and is not exiled")
    void resolvesFromGraveyardOntoBattlefield() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Gravecrawler"));
    }

    @Test
    @DisplayName("Gravecrawler cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent gravecrawler = new Permanent(new Gravecrawler());
        gravecrawler.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(gravecrawler);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
