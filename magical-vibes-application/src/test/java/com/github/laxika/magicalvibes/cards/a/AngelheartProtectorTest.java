package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoilEruption;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelheartProtector.class, DoomBlade.class, GrizzlyBears.class,
        RoilEruption.class, VanquishTheWeak.class})
class AngelheartProtectorTest extends BaseCardTest {

    @Test
    void grantsIndestructibleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelheartProtector()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelheartProtector()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void canTargetItselfWhenEnteringAnEmptyBattlefield() {
        Permanent protector = harness.enterBattlefieldAndReturn(player1, new AngelheartProtector());
        harness.handlePermanentChosen(player1, protector.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, protector, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void triggerResolvesAfterSourceIsDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelheartProtector());
        harness.setHand(player1, List.of(new AngelheartProtector()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanents(player1, "Angelheart Protector").stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Angelheart Protector");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void doesNotProtectTargetBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelheartProtector());
        harness.setHand(player1, List.of(new AngelheartProtector()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanents(player1, "Angelheart Protector").stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(source);
        harness.assertInGraveyard(player1, "Angelheart Protector");
        assertThat(gqs.hasKeyword(gd, source, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void survivesLethalDamageAndCleanupRemovesDamageWithIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelheartProtector());
        harness.setHand(player1, List.of(new AngelheartProtector()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
