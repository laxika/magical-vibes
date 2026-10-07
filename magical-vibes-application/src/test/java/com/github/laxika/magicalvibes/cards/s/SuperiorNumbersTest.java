package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperiorNumbers.class, MtendaHerder.class, WildElephant.class, ManaPrism.class, DarkBanishing.class})
class SuperiorNumbersTest extends BaseCardTest {

    @Test
    @DisplayName("Superior Numbers deals exactly the excess creature count as damage")
    void dealsExactDamageForExcessCreatureCount() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Superior Numbers deals no damage when creature counts are equal")
    void dealsNoDamageWhenCreatureCountsAreEqual() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player2, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Superior Numbers deals only the positive excess")
    void dealsOnlyPositiveExcess() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player2, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Superior Numbers can target a creature you control and a target opponent")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new WildElephant());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        harness.assertInGraveyard(player1, "Wild Elephant");
    }

    @Test
    @DisplayName("Superior Numbers requires a target creature and a target opponent")
    void requiresTargetOpponent() {
        Permanent target = addCreatureReady(player2, new WildElephant());

        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Superior Numbers cannot target its controller as the target opponent")
    void cannotTargetControllerAsOpponent() {
        Permanent target = addCreatureReady(player2, new WildElephant());

        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(target.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Superior Numbers cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaPrism());

        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(target.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Superior Numbers deals no damage when the opponent controls more creatures")
    void dealsNoDamageWhenOpponentHasMoreCreatures() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player2, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Wild Elephant");
    }

    @Test
    @DisplayName("Superior Numbers ignores noncreature permanents in both counts")
    void ignoresNoncreaturePermanents() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        harness.addToBattlefield(player1, new ManaPrism());
        harness.addToBattlefield(player1, new ManaPrism());
        harness.addToBattlefield(player2, new ManaPrism());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player2.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Superior Numbers uses creature counts at resolution after a creature is destroyed")
    void countsCreaturesAtResolution() {
        Permanent removed = addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, removed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mtenda Herder");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Superior Numbers");
    }

    @Test
    @DisplayName("Superior Numbers does not damage another creature when its creature target leaves")
    void doesNotDamageAnotherCreatureWhenTargetLeaves() {
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new MtendaHerder());
        Permanent target = addCreatureReady(player2, new WildElephant());
        Permanent survivor = addCreatureReady(player2, new WildElephant());
        harness.setHand(player1, List.of(new SuperiorNumbers()));
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wild Elephant");
        assertThat(survivor.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor).doesNotContain(target);
        harness.assertInGraveyard(player1, "Superior Numbers");
    }
}
