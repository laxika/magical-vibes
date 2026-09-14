package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.r.RebornHero;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PayNoHeed.class, RebornHero.class, FieryTemper.class})
class PayNoHeedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts for a source choice and shields it globally")
    void chosenSourcePreventedGlobally() {
        Permanent attacker = addCreatureReady(player2, new RebornHero());
        castPayNoHeed();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Chosen source deals no combat damage to a player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new RebornHero());
        castPayNoHeed();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chosen source deals no combat damage to a blocking creature")
    void preventsCombatDamageToCreature() {
        Permanent attacker = addCreatureReady(player2, new RebornHero());
        Permanent blocker = addCreatureReady(player1, new RebornHero());
        castPayNoHeed();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Reborn Hero");
    }

    @Test
    @DisplayName("An unchosen source still deals its damage")
    void unchosenSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        Permanent chosen = addCreatureReady(player2, new RebornHero());
        Permanent other = addCreatureReady(player2, new RebornHero());
        castPayNoHeed();
        harness.handlePermanentChosen(player1, chosen.getId());

        chosen.setAttacking(true);
        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Chosen spell source deals no damage to a player")
    void preventsDamageFromChosenSpellSource() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.castFromHand(player2, new PayNoHeed(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        UUID fieryTemperId = gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof FieryTemper)
                .findFirst()
                .orElseThrow()
                .getTargetableId();
        harness.handlePermanentChosen(player2, fieryTemperId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player2, new RebornHero());
        castPayNoHeed();
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    private void castPayNoHeed() {
        harness.castFromHand(player1, new PayNoHeed(), "{W}");
        harness.passBothPriorities();
    }
}
