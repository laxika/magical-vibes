package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.cards.g.GiltLeafAmbush;
import com.github.laxika.magicalvibes.cards.o.OakenBrawler;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WrensRunVanquisher.class, ElvishEulogist.class, GiltLeafAmbush.class, OakenBrawler.class})
class WrensRunVanquisherTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Elf in hand it costs {1}{G} plus the additional {3}")
    void requiresExtraThreeWithoutElf() {
        // The Vanquisher itself is an Elf but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new WrensRunVanquisher()));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G} only

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Elf is revealed")
    void payTheThreeWithMana() {
        WrensRunVanquisher vanquisher = new WrensRunVanquisher();
        harness.castFromHand(player1, vanquisher, "{4}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vanquisher.getId()));
    }

    @Test
    @DisplayName("Revealing an Elf card from hand lets it be cast for just {1}{G}")
    void revealElfAvoidsTheThree() {
        WrensRunVanquisher vanquisher = new WrensRunVanquisher();
        ElvishEulogist elfInHand = new ElvishEulogist();
        harness.setHand(player1, List.of(vanquisher, elfInHand));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G} only

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vanquisher.getId()));
        // Revealing does not remove the Elf card from hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elfInHand.getId()));
    }

    @Test
    void revealedElfIsShownToOpponentWhenCasting() {
        ElvishEulogist elf = new ElvishEulogist();
        harness.setHand(player1, List.of(new WrensRunVanquisher(), elf));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.clearMessages();

        harness.castCreature(player1, 0);

        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains(elf.getId().toString()));
    }

    @Test
    void kindredElfCardCanSatisfyRevealCost() {
        GiltLeafAmbush elf = new GiltLeafAmbush();
        harness.setHand(player1, List.of(new WrensRunVanquisher(), elf));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wren's Run Vanquisher");
        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
    }

    @Test
    void nonElfInHandDoesNotSatisfyRevealCost() {
        harness.setHand(player1, List.of(new WrensRunVanquisher(), new OakenBrawler()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentElfDoesNotSatisfyRevealCost() {
        harness.setHand(player1, List.of(new WrensRunVanquisher()));
        harness.setHand(player2, List.of(new ElvishEulogist()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void battlefieldElfDoesNotSatisfyRevealCost() {
        harness.setHand(player1, List.of(new WrensRunVanquisher()));
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealingElfDoesNotReplaceRequiredGreenMana() {
        harness.setHand(player1, List.of(new WrensRunVanquisher(), new ElvishEulogist()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchDestroysBlockerWithMoreToughnessThanDamage() {
        addCreatureReady(player1, new WrensRunVanquisher());
        harness.addToBattlefield(player2, new OakenBrawler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Oaken Brawler");
        harness.assertNotOnBattlefield(player2, "Oaken Brawler");
        harness.assertOnBattlefield(player1, "Wren's Run Vanquisher");
    }
}
