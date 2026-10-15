package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StressDream.class, AvatarOfMight.class, GrizzlyBears.class, LlanowarElves.class})
class StressDreamTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player p) {
        harness.addMana(p, ManaColor.BLUE, 1);
        harness.addMana(p, ManaColor.RED, 1);
        harness.addMana(p, ManaColor.GREEN, 3); // 3 generic
    }

    @Test
    @DisplayName("Deals 5 to target creature; chosen card to hand, other to bottom")
    void damagesCreatureAndSelectsCard() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = avatar.getId();
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2, new GrizzlyBears()));

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        // Choose top1 to hand; top2 goes to the bottom
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        GameData gd = harness.getGameData();
        // Avatar (8/8) survives 5 damage
        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        // Chosen card in hand
        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        // Other card on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top2);
    }

    @Test
    @DisplayName("Can be cast with no creature target; still looks at two and takes one")
    void castWithNoCreatureTarget() {
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2, new GrizzlyBears()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId())); // keep top2 instead

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top1);
        harness.assertInGraveyard(player1, "Stress Dream");
    }

    @Test
    void putsOnlyLibraryCardIntoHand() {
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Stress Dream");
    }

    @Test
    void killsTargetAndStillSelectsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2));
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top2);
    }

    @Test
    void dealsDamageWithEmptyLibrary() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Stress Dream");
    }

    @Test
    void doesNotSelectCardsWhenOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2));
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top1, top2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Stress Dream");
    }
}
