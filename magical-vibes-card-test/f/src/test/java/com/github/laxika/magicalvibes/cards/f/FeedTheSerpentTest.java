package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ToskiBearerOfSecrets;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedTheSerpent.class, GrizzlyBears.class, GarrukWildspeaker.class, Forest.class,
        ToskiBearerOfSecrets.class})
class FeedTheSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature")
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a target planeswalker")
    void exilesTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        cast(target);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk Wildspeaker"));
    }

    @Test
    @DisplayName("Rejects a noncreature nonplaneswalker target")
    void rejectsNoncreatureNonplaneswalkerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FeedTheSerpent()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles an indestructible creature")
    void exilesIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ToskiBearerOfSecrets());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Toski, Bearer of Secrets");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertNotInGraveyard(player2, "Toski, Bearer of Secrets");
        harness.assertInGraveyard(player1, "Feed the Serpent");
    }

    @Test
    @DisplayName("Can exile a creature controlled by the caster")
    void exilesOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ToskiBearerOfSecrets());
        cast(target);

        harness.assertNotOnBattlefield(player1, "Toski, Bearer of Secrets");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Toski, Bearer of Secrets");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new FeedTheSerpent()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
