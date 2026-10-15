package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauntlessAven.class, FeralProwler.class, Plains.class})
class DauntlessAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps the chosen creature you control")
    void attackUntapsTargetCreature() {
        addReady(player1, new DauntlessAven());
        Permanent creature = addReady(player1, new FeralProwler());
        creature.tap();
        assertThat(creature.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReady(player1, new DauntlessAven());
        Permanent opponentCreature = addReady(player2, new FeralProwler());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can untap Dauntless Aven itself")
    void canTargetItself() {
        Permanent aven = addReady(player1, new DauntlessAven());

        declareAttackers(player1, List.of(0));
        assertThat(aven.isTapped()).isTrue();
        harness.handlePermanentChosen(player1, aven.getId());
        harness.passBothPriorities();

        assertThat(aven.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped creature is a legal target")
    void canTargetUntappedCreature() {
        Permanent aven = addReady(player1, new DauntlessAven());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(aven.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReady(player1, new DauntlessAven());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent aven = addReady(player1, new DauntlessAven());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        creature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aven);
        gd.playerGraveyards.get(player1.getId()).add(aven.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A target that changes controller before resolution is not untapped")
    void doesNotUntapTargetNoLongerControlled() {
        addReady(player1, new DauntlessAven());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        creature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
