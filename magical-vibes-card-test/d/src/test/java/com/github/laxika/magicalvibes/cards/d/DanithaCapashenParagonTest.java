package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanithaCapashenParagon.class, BondsOfFaith.class, GrizzlyBears.class,
        HeartlessSummoning.class, SylvokLifestaff.class, DeepFreeze.class})
class DanithaCapashenParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Danitha loses her cost reduction when enchanted by Deep Freeze")
    void abilityRemovalStopsCostReduction() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaCapashenParagon());
        harness.setHand(player2, List.of(new DeepFreeze()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, danitha.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, danitha.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Danitha in the graveyard does not reduce Aura costs")
    void graveyardDoesNotProvideReduction() {
        harness.setGraveyard(player1, List.of(new DanithaCapashenParagon()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Generic reduction cannot pay an Aura's colored mana cost")
    void reductionDoesNotReplaceColoredMana() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaCapashenParagon());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, danitha.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Danitha does not reduce equip activation costs")
    void equipCostIsNotReduced() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaCapashenParagon());
        harness.addToBattlefield(player1, new SylvokLifestaff());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, danitha.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Danitha attacks without tapping and gains life from combat damage")
    void vigilanceAndLifelinkInCombat() {
        Permanent danitha = addCreatureReady(player1, new DanithaCapashenParagon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(danitha.isTapped()).isFalse();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Danitha kills a 2/2 blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        Permanent danitha = addCreatureReady(player1, new DanithaCapashenParagon());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Danitha Capashen, Paragon");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(danitha.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Aura spells cost {1} less to cast with Danitha on the battlefield")
    void auraSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        // Bonds of Faith costs {1}{W} — with {1} reduction it should cost just {W}
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bonds of Faith");
    }

    @Test
    @DisplayName("Cannot cast Aura without enough mana even with Danitha's cost reduction")
    void cannotCastAuraWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        // Bonds of Faith costs {1}{W} — with {1} reduction needs {W}; no mana is not enough
        harness.setHand(player1, List.of(new BondsOfFaith()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipment spells cost {1} less to cast with Danitha on the battlefield")
    void equipmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Sylvok Lifestaff costs {1} — with {1} reduction it should cost {0}
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Non-Aura enchantment spells are not reduced by Danitha")
    void nonAuraEnchantmentsNotReduced() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Heartless Summoning costs {1}{B} — should not be reduced since it's not an Aura
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only {B} is not enough for {1}{B}
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature spells are not reduced by Danitha")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Danithas controlled by different players do not combine their reductions")
    void opposingDanithasDoNotCombineReduction() {
        harness.addToBattlefield(player2, new DanithaCapashenParagon());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Danitha does not reduce opponent's Aura spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // Opponent's Bonds of Faith should still cost {1}{W}
        harness.setHand(player2, List.of(new BondsOfFaith()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        // Only {W} is not enough for {1}{W} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
