package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.h.Hex;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnheloThePainter.class, FyndhornElves.class, GrizzlyBears.class, LightningBolt.class, Ponder.class, Hex.class})
class AnheloThePainterTest extends BaseCardTest {

    @Test
    @DisplayName("The first instant or sorcery each turn has casualty 2")
    void firstInstantOrSorceryHasCasualtyTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(fodder.getId()));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().stream().anyMatch(CopyControllerCastSpellEffect.class::isInstance));
    }

    @Test
    @DisplayName("Anhelo's casualty cannot be paid with a creature below power 2")
    void casualtyRequiresPowerTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
    }

    @Test
    @DisplayName("Only the first instant or sorcery each turn gets casualty")
    void onlyFirstInstantOrSorceryGetsCasualty() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(fodder.getId()));
    }

    @Test
    void sacrificingAnheloDoesNotTriggerTheGrantedCasualty() {
        Permanent anhelo = harness.addToBattlefieldAndReturn(player1, new AnheloThePainter());
        anhelo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(anhelo.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(anhelo);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void casualtyCopyResolvesBeforeOriginalAndCanKeepItsTarget() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LightningBolt).hasSize(1);
    }

    @Test
    void casualtyCopyCanChooseANewTarget() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(newTarget);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void enteringAfterAnInstantWasCastDoesNotGrantCasualtyToTheNextSpell() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    void creatureSpellDoesNotUseUpTheFirstInstantOrSorcery() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }

    @Test
    @CardUsed({AnheloThePainter.class, Ponder.class, GrizzlyBears.class})
    void firstSorceryHasCasualty() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castWithCasualty(player1, 0, null, List.of(fodder.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.passBothPriorities();
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    void casualtyIsAvailableAgainOnTheOpponentsTurn() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }

    @Test
    void opponentsSpellDoesNotGetCasualtyOrConsumeControllersGrant() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent ownFodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingFodder = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player2, 0, player1.getId(), List.of(opposingFodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithCasualty(player1, 0, player2.getId(), List.of(ownFodder.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingFodder);
    }

    @Test
    @CardUsed({AnheloThePainter.class, Hex.class, GrizzlyBears.class})
    void multipleTargetCasualtyCopyOffersNewTargets() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<java.util.UUID> targets = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hex()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCardWithCasualty(gd, player1, 0, 0, null, null,
                targets, List.of(), false, null, null, null, null, null, false, null, null,
                List.of(), List.of(), List.of(), false, null, null, null, List.of(), List.of(), null,
                List.of(fodder.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
