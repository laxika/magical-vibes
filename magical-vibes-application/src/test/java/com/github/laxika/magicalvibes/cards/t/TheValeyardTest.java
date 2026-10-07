package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CyberConversion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PleaForPower;
import com.github.laxika.magicalvibes.cards.s.SycoraxCommander;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheValeyard.class, PleaForPower.class, SycoraxCommander.class, Forest.class,
        CyberConversion.class})
class TheValeyardTest extends BaseCardTest {

    @Test
    void opponentFacesVillainousChoiceAnAdditionalTime() {
        harness.addToBattlefield(player1, new TheValeyard());
        harness.setHand(player1, List.of(new SycoraxCommander()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        chooseSycoraxDamage();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        chooseSycoraxDamage();

        harness.assertLife(player2, 14);
    }

    @Test
    void controllerMayVoteAnAdditionalTime() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player1, new TheValeyard());
        harness.setHand(player1, List.of(new PleaForPower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    private void chooseSycoraxDamage() {
        harness.handleListChoice(player2,
                com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect
                        .DAMAGE_OPTION);
    }

    @Test
    void controllerGetsAdditionalVoteDuringOpponentsSpell() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player2, new TheValeyard());
        harness.setHand(player1, List.of(new PleaForPower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void opponentMayChooseDifferentOptionsAndDamageUsesUpdatedHandSize() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.addToBattlefield(player1, new TheValeyard());
        harness.setHand(player1, List.of(new SycoraxCommander()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        chooseSycoraxDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void faceDownValeyardDoesNotRepeatVillainousChoice() {
        Permanent valeyard = harness.addToBattlefieldAndReturn(player1, new TheValeyard());
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, valeyard.getId());
        harness.setHand(player1, List.of(new SycoraxCommander()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        chooseSycoraxDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.PleaForPowerChoice.OPTIONS);
        return choice;
    }
}
