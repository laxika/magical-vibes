package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({UnifiedStrike.class, GlorySeeker.class})
class UnifiedStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an attacking creature whose power is at most the battlefield Soldier count")
    void exilesAttackerWithinSoldierCount() {
        addCreatureReady(player1, new GlorySeeker());
        Permanent attacker = addAttacker(player2, new GlorySeeker());

        cast(attacker);

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Glory Seeker");
    }

    @Test
    @DisplayName("Does not exile an attacking creature whose power exceeds the battlefield Soldier count")
    void doesNotExileAttackerAboveSoldierCount() {
        Permanent attacker = addAttacker(player2, new GlorySeeker());

        cast(attacker);

        harness.assertOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses the Soldier count when the spell resolves")
    void usesSoldierCountAtResolution() {
        Permanent attacker = addAttacker(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        addCreatureReady(player1, new GlorySeeker());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Glory Seeker");
    }

    @Test
    @DisplayName("Does not resolve when the target stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent attacker = addAttacker(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Glory Seeker");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnifiedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player controller, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(controller, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
