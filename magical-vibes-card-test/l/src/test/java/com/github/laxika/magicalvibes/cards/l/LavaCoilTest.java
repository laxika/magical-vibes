package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.d.DeadlyVisit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaCoil.class, AvatarOfMight.class, GrizzlyBears.class, SiegeWurm.class, DeadlyVisit.class})
class LavaCoilTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a small creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LavaCoil()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 4 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new LavaCoil()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(avatar.getMarkedDamage()).isEqualTo(4);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LavaCoil()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature destroyed later that turn is exiled")
    void exilesSurvivorDestroyedLaterThatTurn() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new LavaCoil(), new DeadlyVisit()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, wurm.getId());
        harness.assertOnBattlefield(player2, "Siege Wurm");
        harness.castAndResolveSorcery(player1, 0, wurm.getId());

        harness.assertNotOnBattlefield(player2, "Siege Wurm");
        harness.assertNotInGraveyard(player2, "Siege Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getCard().getId()));
    }

    @Test
    @DisplayName("The exile replacement expires before the next turn")
    void survivorDestroyedNextTurnGoesToGraveyard() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new LavaCoil()));
        harness.setHand(player2, List.of(new DeadlyVisit()));
        harness.setLibrary(player2, List.of(new LavaCoil()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, wurm.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player2, 0, wurm.getId());

        harness.assertNotOnBattlefield(player2, "Siege Wurm");
        harness.assertInGraveyard(player2, "Siege Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
