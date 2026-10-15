package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.s.SignInBlood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
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

@CardUsed({HeadOfTheClass.class, BarkshellBlessing.class, DoomBlade.class, GrizzlyBears.class, TreetopVillage.class, Deathmark.class, SignInBlood.class})
class HeadOfTheClassTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature-targeting spell each turn costs one white and one black less")
    void reducesFirstCreatureTargetingSpellOnly() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstTargetId = harness.getPermanentId(player1, "Grizzly Bears");
        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .toList();
        UUID secondTargetId = bears.get(1).getId();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castInstant(player1, 0, firstTargetId);
            resolveAllTriggers();
        });

        assertThatThrownBy(() -> harness.castInstant(player1, 0, secondTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repartee perpetually boosts Head of the Class")
    void reparteePerpetuallyBoostsThisCreature() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castInstant(player1, 0, bearId);
            resolveAllTriggers();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(3);
    }

    @Test
    @DisplayName("The discount does not pay generic mana")
    void doesNotReduceGenericMana() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discount applies only during your own turn")
    void doesNotReduceCostDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repartee triggers for each qualifying spell, including during an opponent's turn")
    void reparteeTriggersForEverySpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing(), new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castInstant(player1, 0, bearId);
            resolveAllTriggers();
            harness.castInstant(player1, 0, bearId);
            resolveAllTriggers();
        });

        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent-cast spells do not trigger repartee")
    void opponentsSpellDoesNotTriggerRepartee() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new BarkshellBlessing()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            resolveAllTriggers();
        });

        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(2);
    }

    @Test
    @DisplayName("The first spell targeting an animated land receives the discount")
    void discountsSpellTargetingAnimatedLand() {
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID villageId = harness.getPermanentId(player1, "Treetop Village");

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            resolveAllTriggers();
            harness.castInstant(player1, 0, villageId);
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Treetop Village");
        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(3);
    }

    @Test
    @DisplayName("A spell targeting an animated land consumes the first-spell discount")
    void targetingAnimatedLandConsumesDiscount() {
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing(), new DoomBlade()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        UUID villageId = harness.getPermanentId(player1, "Treetop Village");

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            resolveAllTriggers();
            harness.castInstant(player1, 0, villageId);
            resolveAllTriggers();
        });

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature-targeting sorcery receives the discount and triggers repartee")
    void discountsSorceryAndTriggersRepartee() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathmark()));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(3);
    }

    @Test
    @DisplayName("A player-targeting spell neither receives nor consumes the discount and does not trigger repartee")
    void playerTargetingSpellDoesNotUseEitherAbility() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SignInBlood(), new Deathmark()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player1, 0, player1.getId());
            resolveAllTriggers();
            Permanent head = findPermanent(player1, "Head of the Class");
            assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(2);
            harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(3);
    }
}
