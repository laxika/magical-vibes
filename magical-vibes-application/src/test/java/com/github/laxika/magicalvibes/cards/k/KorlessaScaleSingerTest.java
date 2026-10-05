package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DragonbornLooter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorlessaScaleSinger.class, DragonbornLooter.class, GrizzlyBears.class})
class KorlessaScaleSingerTest extends BaseCardTest {

    @Test
    void castsDragonFromLibraryTop() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card dragon = new DragonbornLooter();
        harness.setLibrary(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Dragonborn Looter");
    }

    @Test
    void cannotCastNonDragonFromLibraryTop() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void topCardIsPrivateAndVisibleEvenWhenNotADragon() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void korlessaInLibraryDoesNotRevealItself() {
        Card top = new KorlessaScaleSinger();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void canCastSuccessiveDragonsInTheSameTurn() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card first = new DragonbornLooter();
        Card second = new DragonbornLooter();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
    }

    @Test
    void dragonStillRequiresCreatureTiming() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card dragon = new DragonbornLooter();
        harness.setLibrary(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    void dragonCannotBeCastWithoutPayingItsManaCost() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card dragon = new DragonbornLooter();
        harness.setLibrary(player1, List.of(dragon));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    void opponentsKorlessaDoesNotGrantCastingPermission() {
        harness.addToBattlefield(player2, new KorlessaScaleSinger());
        harness.setLibrary(player1, List.of(new DragonbornLooter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionEndsWhenKorlessaLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card top = new DragonbornLooter();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }
}
