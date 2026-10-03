package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShamblingGhast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mordenkainen;
import com.github.laxika.magicalvibes.cards.a.AdultGoldDragon;
import com.github.laxika.magicalvibes.cards.g.Greataxe;
import com.github.laxika.magicalvibes.cards.k.KalainReclusivePainter;
import com.github.laxika.magicalvibes.cards.l.LolthSpiderQueen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineSmite.class, ShamblingGhast.class, Forest.class, Mordenkainen.class,
        AdultGoldDragon.class, Greataxe.class, KalainReclusivePainter.class, LolthSpiderQueen.class})
class DivineSmiteTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out a nonblack opposing creature")
    void phasesOutNonblackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdultGoldDragon());

        castOn(target.getId());

        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles a black opposing creature")
    void exilesBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShamblingGhast());

        castOn(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can target an opposing planeswalker")
    void phasesOutOpposingPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mordenkainen());
        target.setCounterCount(CounterType.LOYALTY, 3);

        castOn(target.getId());

        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AdultGoldDragon());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void exilesBlackPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LolthSpiderQueen());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castOn(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).doesNotContain(target);
    }

    @Test
    void exilesMulticoloredCreatureThatIsBlack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KalainReclusivePainter());

        castOn(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void phasesAttachmentsOutAndBackInWithTheirHost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdultGoldDragon());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Greataxe());
        equipment.setAttachedTo(target.getId());

        castOn(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(equipment);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void doesNothingWhenTargetChangesToCastersControlBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShamblingGhast());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(target);
    }

    private void castOn(UUID targetId) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new DivineSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
