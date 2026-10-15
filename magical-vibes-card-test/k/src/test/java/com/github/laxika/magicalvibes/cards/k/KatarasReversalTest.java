package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HookSwords;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VigilantDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KatarasReversal.class, GrizzlyBears.class, AngelsMercy.class, VigilantDrake.class, Island.class,
        HookSwords.class, KindlyCustomer.class})
class KatarasReversalTest extends BaseCardTest {

    @Test
    void countersSpellsAndUntapsArtifactsOrCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature1.tap();
        creature2.tap();

        KatarasReversal reversal = new KatarasReversal();
        harness.setHand(player1, List.of(bears, mercy, reversal));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0,
                List.of(bears.getId(), mercy.getId(), creature1.getId(), creature2.getId()));

        assertThat(harness.getGameData().stack.getLast().getTargetGroupSizes()).containsExactly(2, 2);
        harness.passBothPriorities();

        assertThat(creature1.isTapped()).isFalse();
        assertThat(creature2.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Angel's Mercy");
    }

    @Test
    void countersAnActivatedAbility() {
        VigilantDrake drakeCard = new VigilantDrake();
        harness.addToBattlefield(player1, drakeCard);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(drakeCard.getId()));

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Katara's Reversal");
    }

    @Test
    void countersMultipleAbilitiesFromTheSamePermanent() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new VigilantDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        List<UUID> targets = gd.stack.stream().map(entry -> entry.getTargetableId()).toList();
        harness.setHand(player1, List.of(new KatarasReversal()));

        harness.castAndResolveInstant(player1, 0, targets);

        assertThat(gd.stack).isEmpty();
        assertThat(drake.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Vigilant Drake");
    }

    @Test
    void canResolveWithoutAnyTargets() {
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Katara's Reversal");
    }

    @Test
    void untapsFourArtifactsAndCreaturesWithoutCounterTargets() {
        Permanent artifact1 = harness.addToBattlefieldAndReturn(player1, new HookSwords());
        Permanent artifact2 = harness.addToBattlefieldAndReturn(player2, new HookSwords());
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new KindlyCustomer());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new KindlyCustomer());
        List<Permanent> targets = List.of(artifact1, artifact2, creature1, creature2);
        targets.forEach(Permanent::tap);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new HookSwords());
        unchosen.tap();
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targets.stream().map(Permanent::getId).toList());

        targets.forEach(target -> assertThat(target.isTapped()).isFalse());
        assertThat(unchosen.isTapped()).isTrue();
    }

    @Test
    void countersFourSpellsAndUntapsFourPermanents() {
        List<KatarasReversal> spells = List.of(new KatarasReversal(), new KatarasReversal(),
                new KatarasReversal(), new KatarasReversal());
        List<Permanent> permanents = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Permanent permanent = harness.addToBattlefieldAndReturn(player2, new HookSwords());
            permanent.tap();
            permanents.add(permanent);
        }
        List<Card> hand = new ArrayList<>(spells);
        hand.add(new KatarasReversal());
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLUE, 10);
        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0);
        }
        List<UUID> targets = new ArrayList<>(spells.stream().map(KatarasReversal::getId).toList());
        targets.addAll(permanents.stream().map(Permanent::getId).toList());

        harness.castAndResolveInstant(player1, 0, targets);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        permanents.forEach(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    void countersATriggeredAbilityWithoutRemovingItsSource() {
        harness.setLibrary(player2, List.of(new KindlyCustomer()));
        harness.enterBattlefieldAndReturn(player2, new KindlyCustomer());
        UUID triggerId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(triggerId));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Kindly Customer");
    }

    @Test
    void stillUntapsWhenItsCounterTargetLeavesTheStack() {
        Permanent equipment = harness.enterBattlefieldAndReturn(player1, new HookSwords());
        equipment.tap();
        UUID triggerId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new KatarasReversal(), new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, List.of(triggerId, equipment.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(triggerId));

        harness.passBothPriorities();

        assertThat(equipment.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(equipment);
    }

    @Test
    void rejectsFivePermanentTargets() {
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player1, new HookSwords()).getId());
        }
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsFiveSpellTargets() {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            hand.add(new KatarasReversal());
        }
        List<UUID> targets = hand.subList(0, 5).stream().map(Card::getId).toList();
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLUE, 12);
        for (int i = 0; i < 5; i++) {
            harness.castInstant(player1, 0);
        }

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUntapAPlainLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
