package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LathlissDragonQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Dragon creates a 5/5 flying Dragon token")
    void anotherNontokenDragonCreatesToken() {
        Permanent lathliss = harness.addToBattlefieldAndReturn(player1, new LathlissDragonQueen());
        castDragonEgg(player1);

        List<Permanent> dragons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.DRAGON))
                .toList();

        assertThat(dragons).hasSize(3);
        Permanent token = dragons.stream().filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Dragon");
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gd.stack).isEmpty();
        assertThat(lathliss.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Lathliss entering does not trigger its own ability")
    void ownEntryDoesNotTrigger() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new LathlissDragonQueen()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-Dragon creatures do not trigger Lathliss")
    void nonDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new LathlissDragonQueen());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Dragon does not trigger Lathliss")
    void opponentDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new LathlissDragonQueen());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castDragonEgg(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating Lathliss gives Dragons you control +1/+0 until end of turn")
    void activationBoostsOwnDragonsUntilEndOfTurn() {
        Permanent lathliss = harness.addToBattlefieldAndReturn(player1, new LathlissDragonQueen());
        lathliss.setSummoningSick(false);
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonEgg());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new DragonEgg());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lathliss);
        harness.activateAbility(player1, sourceIndex, null, null);
        harness.passBothPriorities();

        assertThat(lathliss.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(ownBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentDragon.getPowerModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lathliss.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getPowerModifier()).isEqualTo(0);
    }

    private void castDragonEgg(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.setHand(player, List.of(new DragonEgg()));
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
