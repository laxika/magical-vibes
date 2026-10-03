package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Desynchronize.class, ArgothianSprite.class, Island.class})
class DesynchronizeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target on top, then scries 2")
    void putsTargetOnTopThenScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card scryTop = new Island();
        Card scryBottom = new Island();
        harness.setLibrary(player2, List.of(targetTop, targetBottom));
        harness.setLibrary(player1, List.of(scryTop, scryBottom));

        castDesynchronize(target);
        harness.handleListChoice(player2, "Top");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), targetTop, targetBottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryTop, scryBottom);
        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        harness.assertInGraveyard(player1, "Desynchronize");
    }

    @Test
    @DisplayName("Puts the target on the bottom, then scries 2")
    void putsTargetOnBottomThenScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card scryTop = new Island();
        Card scryBottom = new Island();
        harness.setLibrary(player2, List.of(targetTop, targetBottom));
        harness.setLibrary(player1, List.of(scryTop, scryBottom));

        castDesynchronize(target);
        harness.handleListChoice(player2, "Bottom");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetTop, targetBottom, target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryBottom, scryTop);
        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Desynchronize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The owner chooses the destination of an opponent-controlled permanent")
    void ownerChoosesForStolenPermanent() {
        Card creature = new ArgothianSprite();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card ownerTop = new Island();
        harness.setLibrary(player2, List.of(ownerTop));
        harness.setLibrary(player1, List.of());

        castDesynchronize(target);
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownerTop, creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.assertInGraveyard(player1, "Desynchronize");
    }

    @Test
    @DisplayName("Scry can put the caster's returned permanent on the bottom")
    void scriesOwnReturnedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        castDesynchronize(target);
        harness.handleListChoice(player1, "Top");
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, target.getCard());
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.assertInGraveyard(player1, "Desynchronize");
    }

    @Test
    @DisplayName("Does not scry when its only target leaves before resolution")
    void doesNotScryWithLostTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Desynchronize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player2, "Argothian Sprite");
        harness.assertInGraveyard(player1, "Desynchronize");
    }

    private void castDesynchronize(Permanent target) {
        harness.setHand(player1, List.of(new Desynchronize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
    }

}
