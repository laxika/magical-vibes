package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScuttlemuttTest extends BaseCardTest {

    // ===== {T}: Add one mana of any color =====

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and taps Scuttlemutt")
    void manaAbilityAddsChosenColor() {
        Permanent mutt = addReadyScuttlemutt(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mutt.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    // ===== {T}: Target creature becomes the color or colors of your choice =====

    @Test
    @DisplayName("Choosing a single color makes the target only that color until end of turn")
    void singleColorReplacesColors() {
        addReadyScuttlemutt(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // green

        activateColorAbilityAndChoose(bears.getId(), "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing several colors makes the target all of those colors")
    void multipleColorsReplaceColors() {
        addReadyScuttlemutt(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateColorAbilityAndChoose(bears.getId(), "WHITE", "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addReadyScuttlemutt(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // green

        activateColorAbilityAndChoose(bears.getId(), "BLUE", "DONE");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);

        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Color ability cannot target a noncreature permanent")
    void colorAbilityRejectsNoncreatureTarget() {
        addReadyScuttlemutt(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyScuttlemutt(Player player) {
        Permanent perm = new Permanent(new Scuttlemutt());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void activateColorAbilityAndChoose(UUID targetId, String... choices) {
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities(); // resolve the ability -> begins the color choice
        for (String choice : choices) {
            harness.handleListChoice(player1, choice);
        }
    }
}
