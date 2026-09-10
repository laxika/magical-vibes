package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultFodder.class, CatapultCaptain.class, GrizzlyBears.class, WallOfAir.class})
class CatapultFodderTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at beginning of combat with three creatures whose toughness exceeds power")
    void transformsWithThreeHighToughnessCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        addWallOfAir(player1);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(fodder.isTransformed()).isTrue();
        assertThat(fodder.getCard()).isInstanceOf(CatapultCaptain.class);
    }

    @Test
    @DisplayName("Does not transform when fewer than three creatures have greater toughness than power")
    void doesNotTransformWithFewerThanThreeHighToughnessCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at beginning of combat on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        addWallOfAir(player1);

        advanceToCombat(player2);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The back face makes an opponent lose life equal to the sacrificed creature's toughness")
    void backFaceLosesLifeEqualToSacrificedToughness() {
        Permanent captain = addTransformedFodder(player1);
        harness.addToBattlefield(player1, new WallOfAir());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(captain.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Wall of Air");
    }

    @Test
    @DisplayName("The back face can target only an opponent")
    void backFaceRejectsNonOpponentTarget() {
        addTransformedFodder(player1);
        harness.addToBattlefield(player1, new WallOfAir());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedFodder(Player player) {
        CatapultFodder card = new CatapultFodder();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addWallOfAir(Player player) {
        Permanent wall = new Permanent(new WallOfAir());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wall);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
