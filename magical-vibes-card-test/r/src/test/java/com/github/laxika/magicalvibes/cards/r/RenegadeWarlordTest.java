package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.s.SkySpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeWarlord.class, SkySpirit.class, Capsize.class})
class RenegadeWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pumps each other attacking creature but not itself")
    void pumpsOtherAttackers() {
        Permanent warlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());
        Permanent stayHome = addCreatureReady(player1, new SkySpirit());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, stayHome)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attacking Warlord boosts the other attackers once")
    void multipleWarlordsStackTheirTriggers() {
        Permanent firstWarlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent secondWarlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstWarlord)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondWarlord)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger resolves even if the Warlord leaves the battlefield")
    void sourceLeavingDoesNotStopBoost() {
        Permanent warlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        declareAttackers(List.of(0, 1));
        harness.castAndResolveInstant(player1, 0, warlord.getId());
        harness.assertInHand(player1, "Renegade Warlord");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only creatures still attacking when the trigger resolves receive the boost")
    void removedAttackerDoesNotReceiveBoost() {
        Permanent warlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent removedAttacker = addCreatureReady(player1, new SkySpirit());
        Permanent remainingAttacker = addCreatureReady(player1, new SkySpirit());
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        declareAttackers(List.of(0, 1, 2));
        harness.castAndResolveInstant(player1, 0, removedAttacker.getId());
        harness.assertInHand(player1, "Sky Spirit");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, remainingAttacker)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removedAttacker);
        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Warlord that does not attack grants no boost")
    void nonattackingWarlordDoesNotBoost() {
        addCreatureReady(player1, new RenegadeWarlord());
        Permanent attacker = addCreatureReady(player1, new SkySpirit());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }
}
