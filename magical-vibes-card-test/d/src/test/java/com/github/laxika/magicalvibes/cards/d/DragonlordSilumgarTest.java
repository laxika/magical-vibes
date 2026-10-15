package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonlordSilumgar.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, Forest.class,
        Unsummon.class})
class DragonlordSilumgarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of target creature")
    void etbGainsControlOfTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castDragonlordSilumgar(target.getId());

        Permanent silumgar = findPermanent(player1, "Dragonlord Silumgar");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.newestControlEffectFor(target.getId()).sourcePermanentId())
                .isEqualTo(silumgar.getId());
    }

    @Test
    @DisplayName("ETB can target a planeswalker")
    void etbGainsControlOfTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);

        castDragonlordSilumgar(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Stolen creature returns when Dragonlord Silumgar leaves the battlefield")
    void stolenCreatureReturnsWhenSilumgarLeaves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castDragonlordSilumgar(target.getId());
        Permanent silumgar = findPermanent(player1, "Dragonlord Silumgar");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, silumgar.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castDragonlordSilumgar(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("No theft if Silumgar leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonlordSilumgar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        Permanent silumgar = findPermanent(player1, "Dragonlord Silumgar");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, silumgar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
        harness.assertInHand(player1, "Dragonlord Silumgar");
    }

    @Test
    @DisplayName("A target that leaves before the trigger resolves is not stolen")
    void targetLeavesBeforeTriggerResolves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonlordSilumgar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dragonlord Silumgar");
    }

    @Test
    @DisplayName("An already controlled creature is a legal target")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castDragonlordSilumgar(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing and regaining Silumgar does not restart its theft")
    void regainingSilumgarDoesNotRestoreTheft() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castDragonlordSilumgar(target.getId());
        Permanent silumgar = findPermanent(player1, "Dragonlord Silumgar");
        Permanent opposingBolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        opposingBolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingBolas),
                1, null, silumgar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, silumgar);
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();

        Permanent friendlyBolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        friendlyBolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(friendlyBolas),
                1, null, silumgar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(silumgar).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    private void castDragonlordSilumgar(UUID targetId) {
        harness.setHand(player1, List.of(new DragonlordSilumgar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
