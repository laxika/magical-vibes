package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivaWardenOfIce;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JillShivasDominant.class, ShivaWardenOfIce.class, Forest.class, FountainOfYouth.class,
        GrizzlyBears.class})
class JillShivasDominantTest extends BaseCardTest {

    @Test
    void entersAndReturnsAnotherNonlandPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castJill(target.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInHand(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    void enterAbilityCannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new JillShivasDominant()));
        addJillMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent");
    }

    @Test
    void transformsAndResolvesChapterI() {
        castJill();
        Permanent jill = findPermanent(player1, JillShivasDominant.class);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        jill.setSummoningSick(false);

        addTransformMana();
        harness.activateAbility(player1, indexOf(player1, jill), 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent shiva = findPermanent(player1, ShivaWardenOfIce.class);
        assertThat(shiva.isTransformed()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void chapterIIAlsoMakesItsTargetUnblockable() {
        Permanent shiva = addShivaWithLore(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(shiva.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void chapterIIITapsOpponentsLandsAndReturnsShivaToTheFrontFace() {
        Permanent shiva = addShivaWithLore(2);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent jill = findPermanent(player1, JillShivasDominant.class);
        assertThat(jill.isTransformed()).isFalse();
        assertThat(opponentLand.isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shiva);
    }

    private void castJill() {
        harness.setHand(player1, List.of(new JillShivasDominant()));
        addJillMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castJill(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new JillShivasDominant()));
        addJillMana();
        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addJillMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private Permanent addShivaWithLore(int loreCounters) {
        JillShivasDominant card = new JillShivasDominant();
        Permanent shiva = new Permanent(card);
        shiva.setCard(card.getBackFaceCard());
        shiva.setTransformed(true);
        shiva.setSummoningSick(false);
        shiva.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(shiva);
        return shiva;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, Class<?> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
