package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CribSwapTest extends BaseCardTest {

    // ===== Exile creature and give its controller a Shapeshifter token =====

    @Test
    @DisplayName("Exiles target creature and gives its controller a 1/1 colorless Shapeshifter with changeling")
    void exilesCreatureAndCreatesTokenForController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature exiled (not to graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Its controller (player2) gets a 1/1 colorless Shapeshifter token with changeling
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shapeshifter")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == null
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.SHAPESHIFTER)
                        && p.getCard().getKeywords().contains(Keyword.CHANGELING));
    }

    @Test
    @DisplayName("Can target own creature — controller gets the Shapeshifter token")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Controller (player1) gets the token
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shapeshifter")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles when target is removed before resolution — no token created")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shapeshifter"));
    }
}
