package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({DalkovanEncampment.class, GrizzlyBears.class, Mountain.class, Swamp.class})
class DalkovanEncampmentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Swamp or Mountain")
    void entersTappedWithoutQualifyingLand() {
        playEncampment();

        assertThat(findPermanent(player1, "Dalkovan Encampment").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        playEncampment();

        assertThat(findPermanent(player1, "Dalkovan Encampment").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        playEncampment();

        assertThat(findPermanent(player1, "Dalkovan Encampment").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping adds one white mana")
    void tapsForWhiteMana() {
        addEncampmentReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack ability creates two tapped and attacking Warrior tokens")
    void attackingCreatesTwoWarriorTokens() {
        addEncampmentReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        activateAttackAbility();

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoices();

        List<Permanent> tokens = warriorTokens();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("The created tokens are sacrificed at the next end step")
    void createdTokensAreSacrificedAtNextEndStep() {
        addEncampmentReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        activateAttackAbility();

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoices();
        assertThat(warriorTokens()).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warriorTokens()).isEmpty();
    }

    private void playEncampment() {
        harness.setHand(player1, List.of(new DalkovanEncampment()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }

    private Permanent addEncampmentReady(Player player) {
        Permanent permanent = new Permanent(new DalkovanEncampment());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activateAttackAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private void resolveTokenAttackTargetChoices() {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
    }

    private List<Permanent> warriorTokens() {
        return findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
