package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostwalkBastion.class, GiantSpider.class})
class FrostwalkBastionTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Frostwalk Bastion produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent bastion = addReady(player1, new FrostwalkBastion());

        harness.tapPermanent(player1, indexOf(player1, bastion));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation ability requires snow mana")
    void animationRequiresSnowMana() {
        Permanent bastion = addReady(player1, new FrostwalkBastion());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, bastion), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Snow mana animates Frostwalk Bastion as a 2/3 Construct artifact creature that stays a land")
    void animatesAsConstructArtifactCreature() {
        Permanent bastion = addReady(player1, new FrostwalkBastion());
        addAnimationMana(player1);

        harness.activateAbility(player1, indexOf(player1, bastion), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bastion)).isTrue();
        assertThat(gqs.isArtifact(gd, bastion)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bastion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bastion)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, bastion, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.isLand(gd, bastion)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent bastion = addReady(player1, new FrostwalkBastion());
        addAnimationMana(player1);

        harness.activateAbility(player1, indexOf(player1, bastion), 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bastion)).isFalse();
        assertThat(gqs.isArtifact(gd, bastion)).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent bastion = addReady(player1, new FrostwalkBastion());
        addAnimationMana(player1);
        harness.activateAbility(player1, indexOf(player1, bastion), 0, null, null);
        harness.passBothPriorities();
        bastion.setAttacking(true);

        Permanent blocker = addReady(player2, new GiantSpider());
        beginBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, bastion))));
        harness.passBothPriorities();
        resolveStack();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    private void addAnimationMana(Player player) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(ManaColor.COLORLESS, 1);
        pool.addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }
}
