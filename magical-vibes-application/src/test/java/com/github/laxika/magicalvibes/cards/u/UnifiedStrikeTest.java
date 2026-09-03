package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnifiedStrike.class, GrizzlyBears.class, YotianSoldier.class})
class UnifiedStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an attacking creature whose power is at most the battlefield Soldier count")
    void exilesAttackerWithinSoldierCount() {
        addCreatureReady(player2, new YotianSoldier());
        addCreatureReady(player2, new YotianSoldier());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        cast(attacker);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not exile an attacking creature whose power exceeds the battlefield Soldier count")
    void doesNotExileAttackerAboveSoldierCount() {
        addCreatureReady(player2, new YotianSoldier());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        cast(attacker);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        gd.playerBattlefields.get(controller.getId()).add(attacker);
        return attacker;
    }
}
