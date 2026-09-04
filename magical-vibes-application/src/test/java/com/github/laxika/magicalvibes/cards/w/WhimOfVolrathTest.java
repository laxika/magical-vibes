package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WhimOfVolrathTest extends BaseCardTest {

    @Test
    @DisplayName("Replacing a color word records the change on the target permanent")
    void replacesColorWord() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        Permanent perm = findPermanent(player2, "Paladin en-Vec");
        assertThat(perm.getTextReplacements())
                .containsExactly(new TextReplacement("black", "green", true));
        assertThat(graveyardNames(player1)).containsExactly("Whim of Volrath");
    }

    @Test
    @DisplayName("Replacing a basic land type records the change on the target permanent")
    void replacesLandType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        Permanent perm = findPermanent(player2, "Grizzly Bears");
        assertThat(perm.getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Forest", true));
    }

    @Test
    @DisplayName("The text change wears off at end of turn")
    void textChangeWearsOff() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        Permanent perm = findPermanent(player2, "Paladin en-Vec");
        assertThat(perm.getTextReplacements()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(perm.getTextReplacements()).isEmpty();
    }

    @Test
    @DisplayName("A color-word change does not change a color chosen as the permanent entered")
    void textChangeDoesNotChangeChosenColor() {
        harness.addToBattlefield(player2, new VoiceOfAll());
        Permanent voiceOfAll = findPermanent(player2, "Voice of All");
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Voice of All");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        assertThat(voiceOfAll.getChosenColor()).isEqualTo(CardColor.BLACK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(voiceOfAll.getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Paying buyback returns Whim of Volrath to its owner's hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        assertThat(handNames(player1)).containsExactly("Whim of Volrath");
        assertThat(graveyardNames(player1)).isEmpty();
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
