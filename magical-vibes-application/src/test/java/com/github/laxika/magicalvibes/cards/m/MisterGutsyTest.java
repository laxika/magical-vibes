package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MisterGutsy.class, HolyStrength.class, LeoninScimitar.class, GrizzlyBears.class, Murder.class})
class MisterGutsyTest extends BaseCardTest {

    @Test
    void putsCountersOnItselfForAuraAndEquipmentSpells() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, gutsy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ignoresNonAuraAndNonEquipmentSpells() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void createsJunkForEachPlusOnePlusOneCounterWhenItDies() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, gutsy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, gutsy.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void createsNoJunkWhenItDiesWithoutCounters() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, gutsy.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void deathAbilityStillTriggersWithZeroCounters() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, gutsy.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(gutsy.getCard().getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void getsCounterBeforeEquipmentSpellResolves() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    void ignoresOpponentsAuraAndEquipmentSpells() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castEnchantment(player2, 0, gutsy.getId());
        resolveAllTriggers();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void junkSacrificesAsCostAndExilesTopCardWithPaidPlayPermission() {
        Permanent junk = createJunk();
        MisterGutsy top = new MisterGutsy();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null);

        harness.assertNotOnBattlefield(player1, "Junk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.inMutationScope(() -> harness.getSpellCastingService()
                .playCardFromExile(gd, player1, top.getId(), 0, null));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mister Gutsy");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void junkCannotActivateOutsideMainPhase() {
        Permanent junk = createJunk();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Junk");
    }

    @Test
    void tappedJunkCannotActivate() {
        Permanent junk = createJunk();
        junk.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Junk");
    }

    @Test
    void junkCanBeSacrificedWithAnEmptyLibrary() {
        Permanent junk = createJunk();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Junk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Permanent createJunk() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());
        gutsy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, gutsy.getId());
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Junk"))
                .findFirst().orElseThrow();
    }
}
