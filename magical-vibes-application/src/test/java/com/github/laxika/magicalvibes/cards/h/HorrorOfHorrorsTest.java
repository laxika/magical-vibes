package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorrorOfHorrorsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Swamp puts the regeneration ability on the stack targeting the black creature")
    void activatingTargetsBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addReadyCreature(player1, createBlackCreature());
        harness.addToBattlefield(player1, createSwamp());

        harness.activateAbility(player1, 0, null, zombie.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(zombie.getId());
        // Swamp is sacrificed as a cost.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Swamp"));
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target black creature")
    void resolvingGrantsShield() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addReadyCreature(player1, createBlackCreature());
        harness.addToBattlefield(player1, createSwamp());

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent whiteCreature = addReadyCreature(player1, createWhiteCreature());
        harness.addToBattlefield(player1, createSwamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, whiteCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("Cannot activate the ability without a Swamp to sacrifice")
    void cannotActivateWithoutSwamp() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addReadyCreature(player1, createBlackCreature());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createBlackCreature() {
        Card card = new Card();
        card.setName("Zombie");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.ZOMBIE));
        return card;
    }

    private Card createWhiteCreature() {
        Card card = new Card();
        card.setName("Soldier");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private Card createSwamp() {
        Card card = new Card();
        card.setName("Swamp");
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.SWAMP));
        return card;
    }
}
