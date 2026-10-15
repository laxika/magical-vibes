package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LongGoodbye;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeshiftBinding.class, GrizzlyBears.class, Naturalize.class, Forest.class, LongGoodbye.class})
class MakeshiftBindingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent's creature and gains 2 life")
    void etbExilesOpponentCreatureAndGainsLife() {
        harness.setLife(player1, 15);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearsId));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Exiled creature returns when Makeshift Binding leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID bindingId = harness.getPermanentId(player1, "Makeshift Binding");

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bindingId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent's creature can be targeted")
    void rejectsLandAndOwnCreatureTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID landId = harness.getPermanentId(player2, "Forest");
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");

        prepareToCast();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new MakeshiftBinding()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Binding before its trigger resolves still gains life without exiling")
    void sourceLeavesBeforeTriggerResolves() {
        harness.setLife(player1, 15);
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        prepareToCast();
        harness.castEnchantment(player1, 0, creatureId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Makeshift Binding"));
        harness.assertLife(player1, 15);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Makeshift Binding");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal sole target prevents both exile and life gain")
    void targetLeavesBeforeTriggerResolves() {
        harness.setLife(player1, 15);
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        prepareToCast();
        harness.castEnchantment(player1, 0, creatureId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LongGoodbye()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Makeshift Binding");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MakeshiftBinding()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
