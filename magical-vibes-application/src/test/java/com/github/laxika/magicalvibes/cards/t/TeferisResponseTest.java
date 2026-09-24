package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WingsOfHope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisResponse.class, FertileGround.class, Island.class, DreamThrush.class, WingsOfHope.class})
class TeferisResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's land-targeting spell, preserves the land, and draws two cards")
    void countersLandTargetingSpellAndDrawsTwo() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        FertileGround fertileGround = new FertileGround();
        harness.setHand(player2, List.of(fertileGround));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, island.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, fertileGround.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player2, "Fertile Ground");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Counters an opponent's land-targeting ability and destroys its permanent source")
    void countersLandTargetingAbilityAndDestroysSource() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent dreamThrush = addCreatureReady(player2, new DreamThrush());
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        int sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(dreamThrush);
        harness.activateAbility(player2, sourceIndex, null, island.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, dreamThrush.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player2, "Dream Thrush");
    }

    @Test
    @DisplayName("Cannot target a land-targeting spell controlled by its own caster")
    void cannotTargetOwnLandTargetingSpell() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        FertileGround fertileGround = new FertileGround();
        harness.setHand(player1, List.of(fertileGround, new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.castEnchantment(player1, 0, island.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fertileGround.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's land-targeting spell when no land you control is targeted")
    void cannotTargetOpponentLandTargetingSpellThatTargetsOpponentsLand() {
        harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentsIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        FertileGround fertileGround = new FertileGround();
        harness.setHand(player2, List.of(fertileGround));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, opponentsIsland.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fertileGround.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's spell that targets a nonland permanent")
    void cannotTargetOpponentNonLandTargetingSpell() {
        harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent targetCreature = addCreatureReady(player1, new DreamThrush());
        WingsOfHope wingsOfHope = new WingsOfHope();
        harness.setHand(player2, List.of(wingsOfHope));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, targetCreature.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wingsOfHope.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's land-targeting ability when the land is no longer controlled")
    void cannotTargetAbilityAfterItsLandTargetBecomesIllegal() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent dreamThrush = addCreatureReady(player2, new DreamThrush());
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        int sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(dreamThrush);
        harness.activateAbility(player2, sourceIndex, null, island.getId());
        gd.playerBattlefields.get(player1.getId()).remove(island);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, dreamThrush.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles without drawing when the targeted land is removed before resolution")
    void fizzlesWhenTargetLandIsRemovedBeforeResolution() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        FertileGround fertileGround = new FertileGround();
        harness.setHand(player2, List.of(fertileGround));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, island.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, fertileGround.getId());
        gd.playerBattlefields.get(player1.getId()).remove(island);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(fertileGround.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fertile Ground");
        assertThat(gd.stack).isEmpty();
    }
}
