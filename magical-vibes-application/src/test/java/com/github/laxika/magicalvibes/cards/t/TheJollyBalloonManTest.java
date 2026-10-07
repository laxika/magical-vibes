package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcrobaticCheerleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetStaticaster;
import com.github.laxika.magicalvibes.cards.m.MirrorRoomFracturedRealm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheJollyBalloonMan.class, GrizzlyBears.class, IzzetStaticaster.class,
        AcrobaticCheerleader.class, MirrorRoomFracturedRealm.class})
class TheJollyBalloonManTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a red 1/1 Balloon copy with flying and haste")
    void createsBalloonCopy() {
        addJollyReady(player1);
        Permanent target = addCreatureReady(player1, new IzzetStaticaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.HUMAN, CardSubtype.WIZARD, CardSubtype.BALLOON);
        assertThat(token.getCard().getKeywords())
                .contains(Keyword.FLYING, Keyword.HASTE);
    }

    @Test
    @DisplayName("Sacrifices the token at the beginning of the next end step")
    void sacrificesTokenAtNextEndStep() {
        Permanent jolly = addJollyReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(jolly.getId()));
    }

    @Test
    @DisplayName("Cannot target itself or a creature controlled by an opponent")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent jolly = addJollyReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jolly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addJollyReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A copy of the Balloon retains haste and is not sacrificed with the original")
    void balloonHasteIsCopiableButDelayedSacrificeIsNot() {
        addJollyReady(player1);
        Permanent target = addCreatureReady(player1, new AcrobaticCheerleader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent balloon = findPermanents(player1, "Acrobatic Cheerleader").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, balloon, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new MirrorRoomFracturedRealm()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, balloon.getId());
        resolveAllTriggers();

        Permanent reflection = findPermanents(player1, "Acrobatic Cheerleader").stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && !permanent.getId().equals(balloon.getId())).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.FLYING)).isTrue();
        assertThat(reflection.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.RED);
        assertThat(reflection.getCard().getSubtypes()).contains(CardSubtype.BALLOON, CardSubtype.REFLECTION);
        assertThat(gqs.getEffectivePower(gd, reflection)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, reflection)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reflection).doesNotContain(balloon);
    }

    @Test
    @DisplayName("No token is created if the target leaves before resolution")
    void targetLeavingBattlefieldPreventsCopy() {
        Permanent jolly = addJollyReady(player1);
        Permanent target = addCreatureReady(player1, new AcrobaticCheerleader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(jolly.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addJollyReady(Player player) {
        return addCreatureReady(player, new TheJollyBalloonMan());
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
