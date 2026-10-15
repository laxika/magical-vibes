package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CunningNightbonder;
import com.github.laxika.magicalvibes.cards.e.EssenceSymbiote;
import com.github.laxika.magicalvibes.cards.s.Slitherwisp;
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

@CardUsed({MutualDestruction.class, EssenceSymbiote.class, CunningNightbonder.class, Slitherwisp.class})
class MutualDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and destroys the target creature")
    void sacrificesCreatureAndDestroysTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(sacrifice.getCard());
        harness.assertInGraveyard(player1, "Mutual Destruction");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("Cannot cast without sacrificing a creature")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn while controlling a permanent with flash")
    void canBeCastAtInstantSpeedWithFlashPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast during an opponent's turn without a permanent with flash")
    void cannotBeCastAtInstantSpeedWithoutFlashPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can sacrifice the only flash permanent and still finish casting on an opponent's turn")
    void canSacrificeOnlyFlashPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CunningNightbonder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cunning Nightbonder");
        harness.assertInGraveyard(player1, "Mutual Destruction");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("An opponent's flash permanent does not grant instant timing")
    void opponentsFlashPermanentDoesNotGrantFlash() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CunningNightbonder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(sacrifice);
    }

    @Test
    @DisplayName("Can target the creature sacrificed as the cost, leaving the spell with an illegal target")
    void canTargetSacrificedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Essence Symbiote");
        harness.assertInGraveyard(player1, "Mutual Destruction");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the additional cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Counts as a spell with flash for Slitherwisp while a flash permanent remains")
    void triggersSlitherwispWhileItHasFlash() {
        harness.addToBattlefield(player1, new Slitherwisp());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EssenceSymbiote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        EssenceSymbiote drawn = new EssenceSymbiote();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new MutualDestruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }
}
