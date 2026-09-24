package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hookblade.class, GrizzlyBears.class})
class HookbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it +1/+0")
    void entersAttachedAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hookblade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hookblade = findPermanent(player1, "Hookblade");
        assertThat(hookblade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has flying during its controller's turn")
    void equippedCreatureHasFlyingDuringControllerTurn() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent hookblade = addReady(player1, new Hookblade());
        hookblade.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature loses flying during the opponent's turn")
    void equippedCreatureLosesFlyingDuringOpponentsTurn() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent hookblade = addReady(player1, new Hookblade());
        hookblade.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Hookblade to a creature you control")
    void equipAttachesToCreature() {
        Permanent hookblade = addReady(player1, new Hookblade());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hookblade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("ETB attach cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hookblade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
