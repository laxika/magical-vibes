package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepOfTheDead.class, NyxbornColossus.class, Forest.class})
class SleepOfTheDeadTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureAndSkipsItsNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new SleepOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        target.tap();
        harness.setHand(player1, List.of(new SleepOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SleepOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionExpiresAfterOnlyTheTargetsControllersNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new SleepOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void escapePaysThreeOtherCardsAndReturnsTheSpellToTheGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        SleepOfTheDead spell = new SleepOfTheDead();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of(spell, first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    void escapeRequiresThreeOtherGraveyardCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        SleepOfTheDead spell = new SleepOfTheDead();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player1, List.of(spell, first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void escapeCannotExileTheSpellItselfToPayItsCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        SleepOfTheDead spell = new SleepOfTheDead();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of(spell, first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, target.getId(), List.of(), List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
