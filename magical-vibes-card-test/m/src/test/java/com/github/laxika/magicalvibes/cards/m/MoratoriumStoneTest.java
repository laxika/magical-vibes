package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoratoriumStone.class, GodlessShrine.class, Gristleback.class, FathomSeer.class})
class MoratoriumStoneTest extends BaseCardTest {

    @Test
    void exilesTargetCardFromAnyGraveyard() {
        MoratoriumStone stone = new MoratoriumStone();
        harness.addToBattlefield(player1, stone);
        GodlessShrine target = new GodlessShrine();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertOnBattlefield(player1, "Moratorium Stone");
        assertThat(findPermanent(player1, "Moratorium Stone").isTapped()).isTrue();
    }

    @Test
    void firstAbilityCanExileACardFromItsControllersGraveyard() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        Gristleback target = new Gristleback();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).containsExactly(target);
        harness.assertOnBattlefield(player1, "Moratorium Stone");
    }

    @Test
    void exilesAllMatchingGraveyardCardsAndPermanents() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        Gristleback player1Permanent = new Gristleback();
        Gristleback player2Permanent = new Gristleback();
        harness.addToBattlefield(player1, player1Permanent);
        harness.addToBattlefield(player2, player2Permanent);
        harness.addToBattlefield(player2, new GodlessShrine());

        Gristleback player1Graveyard = new Gristleback();
        Gristleback player2Target = new Gristleback();
        harness.setGraveyard(player1, List.of(player1Graveyard));
        harness.setGraveyard(player2, List.of(player2Target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, 1, null, player2Target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(player1Graveyard, player1Permanent);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(player2Target, player2Permanent);
        harness.assertInGraveyard(player1, "Moratorium Stone");
        harness.assertNotOnBattlefield(player1, "Gristleback");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Godless Shrine");
    }

    @Test
    void secondAbilityCannotTargetALandCard() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        GodlessShrine target = new GodlessShrine();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Moratorium Stone");
    }

    @Test
    void sacrificedStoneIsAlsoExiledWhenTargetingAnotherStone() {
        MoratoriumStone source = new MoratoriumStone();
        MoratoriumStone target = new MoratoriumStone();
        Gristleback unrelated = new Gristleback();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(target, unrelated));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        harness.assertInGraveyard(player1, "Moratorium Stone");
        harness.assertNotOnBattlefield(player1, "Moratorium Stone");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(source, target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unrelated);
    }

    @Test
    void losingTheOnlyTargetLeavesOtherMatchingCardsAndPermanentsAlone() {
        MoratoriumStone source = new MoratoriumStone();
        harness.addToBattlefield(player1, source);
        harness.addToBattlefield(player1, new MoratoriumStone());
        Gristleback target = new Gristleback();
        Gristleback other = new Gristleback();
        harness.setGraveyard(player2, List.of(target));
        harness.setGraveyard(player1, List.of(other));
        harness.addToBattlefield(player2, new Gristleback());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(other, source);
        harness.assertOnBattlefield(player2, "Gristleback");
        harness.assertOnBattlefield(player1, "Moratorium Stone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotExileNamelessFaceDownCreatureWithMatchingUnderlyingCard() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        harness.setHand(player2, List.of(new FathomSeer()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareActivation(player2);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        Permanent faceDown = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst().orElseThrow();
        Permanent faceUp = harness.addToBattlefieldAndReturn(player2, new FathomSeer());
        FathomSeer target = new FathomSeer();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(faceDown);
        assertThat(faceDown.isFaceDown()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(target, faceUp.getCard());
        harness.assertInGraveyard(player1, "Moratorium Stone");
    }

    private void prepareActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
