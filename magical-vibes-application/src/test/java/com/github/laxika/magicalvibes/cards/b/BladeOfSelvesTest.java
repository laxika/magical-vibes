package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeOfSelves.class, GrizzlyBears.class})
class BladeOfSelvesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Blade of Selves attaches it to a creature")
    void equipsToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Accepting myriad creates a tapped attacking copy for another opponent")
    void myriadCreatesCopyForOtherOpponent() {
        UUID otherOpponentId = addOtherOpponent();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(otherOpponentId);
    }

    private Permanent addBladeReady() {
        Permanent blade = new Permanent(new BladeOfSelves());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blade);
        return blade;
    }

    private UUID addOtherOpponent() {
        UUID playerId = UUID.randomUUID();
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Carol");
        gd.playerIdToName.put(playerId, "Carol");
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        return playerId;
    }
}
