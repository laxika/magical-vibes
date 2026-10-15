package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scuttlemutt.class, GrizzlyBears.class, Forest.class})
class ScuttlemuttTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and taps Scuttlemutt")
    void manaAbilityAddsChosenColor() {
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mutt.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a single color makes the target only that color until end of turn")
    void singleColorReplacesColors() {
        addCreatureReady(player1, new Scuttlemutt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // green

        activateColorAbilityAndChoose(bears.getId(), "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing several colors makes the target all of those colors")
    void multipleColorsReplaceColors() {
        addCreatureReady(player1, new Scuttlemutt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateColorAbilityAndChoose(bears.getId(), "WHITE", "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new Scuttlemutt());
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
        addCreatureReady(player1, new Scuttlemutt());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scuttlemutt can target itself and become all five colors")
    void canChooseAllFiveColorsForItself() {
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());

        activateColorAbilityAndChoose(mutt.getId(), "WHITE", "BLUE", "BLACK", "RED", "GREEN");

        assertThat(gqs.getEffectiveColors(gd, mutt))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(mutt.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void summoningSicknessPreventsBothAbilities() {
        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new Scuttlemutt());
        mutt.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mutt.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mutt.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying one tap cost prevents activating the other ability")
    void manaAbilityPreventsSecondActivation() {
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mutt.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectiveColors(gd, mutt)).isEmpty();
    }

    @Test
    @DisplayName("The color ability resolves even after Scuttlemutt leaves the battlefield")
    void colorAbilitySurvivesSourceLeaving() {
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Scuttlemutt());
        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mutt);
        gd.playerGraveyards.get(player1.getId()).add(mutt.getCard());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color ability does nothing when its target leaves before resolution")
    void colorAbilityDoesNotAffectReturningCreature() {
        addCreatureReady(player1, new Scuttlemutt());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Scuttlemutt());
        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, returned)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void activateColorAbilityAndChoose(UUID targetId, String... choices) {
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities(); // resolve the ability -> begins the color choice
        for (String choice : choices) {
            harness.handleListChoice(player1, choice);
        }
    }
}
