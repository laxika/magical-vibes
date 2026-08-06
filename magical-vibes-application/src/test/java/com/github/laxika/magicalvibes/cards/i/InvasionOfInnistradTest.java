package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.f.FiresongAndSunspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDividedDamageEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvasionOfInnistradTest extends BaseCardTest {

    @Nested
    @DisplayName("Front face ETB")
    class FrontFaceEtb {

        @Test
        @DisplayName("Enters with defense counters and gives opponent creature -13/-13")
        void etbWeakenOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            castInvasion(bearsId);
            harness.passBothPriorities(); // resolve battle spell
            harness.passBothPriorities(); // resolve ETB

            Permanent battle = findPermanent(player1, "Invasion of Innistrad");
            assertThat(battle.getCard().hasType(CardType.BATTLE)).isTrue();
            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
            assertThat(battle.getProtectorPlayerId()).isEqualTo(player2.getId());

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> "Grizzly Bears".equals(p.getCard().getName()));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> "Grizzly Bears".equals(c.getName()));
        }

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(new InvasionOfInnistrad()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Siege defeat")
    class SiegeDefeat {

        @Test
        @DisplayName("When defeated, exiles and casts Deluge of the Dead which creates two Zombies")
        void defeatCastsBackFace() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            castInvasion(bearsId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            Permanent battle = findPermanent(player1, "Invasion of Innistrad");
            battle.setCounterCount(CounterType.DEFENSE, 0);
            harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                    .checkAfterDefenseRemoved(gd, battle));

            harness.passBothPriorities(); // resolve defeat trigger (exile + put transformed spell)
            harness.passBothPriorities(); // resolve Deluge spell
            harness.passBothPriorities(); // resolve Deluge ETB

            Permanent deluge = findPermanent(player1, "Deluge of the Dead");
            assertThat(deluge.isTransformed()).isTrue();
            assertThat(deluge.getCard().hasType(CardType.ENCHANTMENT)).isTrue();

            long zombies = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> "Zombie".equals(p.getCard().getName()))
                    .count();
            assertThat(zombies).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Back face ability")
    class BackFaceAbility {

        @Test
        @DisplayName("Exiling a creature card creates a Zombie; exiling a noncreature does not")
        void exileCreatesZombieIfCreature() {
            DelugeOfTheDead delugeCard = new DelugeOfTheDead();
            Permanent deluge = new Permanent(delugeCard);
            deluge.setTransformed(true);
            gd.playerBattlefields.get(player1.getId()).add(deluge);

            gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
            gd.playerGraveyards.get(player2.getId()).add(new Shock());

            UUID creatureGyId = gd.playerGraveyards.get(player2.getId()).stream()
                    .filter(c -> "Grizzly Bears".equals(c.getName()))
                    .findFirst().orElseThrow().getId();
            UUID shockGyId = gd.playerGraveyards.get(player2.getId()).stream()
                    .filter(c -> "Shock".equals(c.getName()))
                    .findFirst().orElseThrow().getId();

            int delugeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(deluge);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, delugeIndex, 0, null, creatureGyId, Zone.GRAVEYARD);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> "Zombie".equals(p.getCard().getName()))
                    .count()).isEqualTo(1);
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> "Grizzly Bears".equals(c.getName()));

            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, delugeIndex, 0, null, shockGyId, Zone.GRAVEYARD);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> "Zombie".equals(p.getCard().getName()))
                    .count()).isEqualTo(1);
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> "Shock".equals(c.getName()));
        }
    }

    /**
     * CR 115.4 lists battles among what "any target" may be, so a burn spell may be aimed at one and
     * the damage removes defense counters (CR 120.3h).
     */
    @Nested
    @DisplayName("Chosen as an \"any target\"")
    class ChosenAsAnyTarget {

        @Test
        @DisplayName("A burn spell can be aimed at a battle, removing that many defense counters")
        void burnSpellTargetsTheBattle() {
            Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, 5);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            var response = harness.getValidTargetService().computeValidTargetsForSpell(
                    gd, gd.playerHands.get(player1.getId()).getFirst(), player1.getId(), null);
            assertThat(response.validPermanentIds()).contains(battle.getId());

            harness.castInstant(player1, 0, battle.getId());
            harness.passBothPriorities();

            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        }

        @Test
        @DisplayName("Removing the last defense counter by targeted damage defeats the Siege")
        void lethalBurnDefeatsTheSiege() {
            Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, 2);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, battle.getId());
            harness.passBothPriorities(); // resolve Shock
            harness.passBothPriorities(); // resolve defeat trigger (exile + put transformed spell)
            harness.passBothPriorities(); // resolve Deluge spell
            harness.passBothPriorities(); // resolve Deluge ETB

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> "Invasion of Innistrad".equals(p.getCard().getName()));
            assertThat(findPermanent(player1, "Deluge of the Dead").isTransformed()).isTrue();
        }
    }

    /**
     * Damage dealt to a battle removes that many defense counters (CR 120.3h). These drive the damage
     * services directly, so the divided-damage entry points are covered without a card that can aim
     * a division at a battle.
     */
    @Nested
    @DisplayName("Damage to a battle")
    class DamageToBattle {

        @Test
        @DisplayName("Divided damage removes defense counters instead of skipping the battle")
        void dividedDamageRemovesDefenseCounters() {
            Permanent battle = addBattle(5);
            DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
            Card source = new Shock();

            harness.inMutationScope(() -> damageSupport.dealDividedDamageToAnyTargets(
                    gd, source, player1.getId(), Map.of(battle.getId(), 3)));

            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        }

        @Test
        @DisplayName("An ordered divided-damage spell removes defense counters from a battle target")
        void orderedDividedDamageRemovesDefenseCounters() {
            Permanent battle = addBattle(5);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            var handler = GameTestEngineContext.get().getBean(DealDividedDamageEffectHandler.class);
            // Arc Trail's shape: 2 damage to one target, 1 to another.
            var effect = DealDividedDamageEffect.ordered(List.of(2, 1));
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, new ArcTrail(),
                    player1.getId(), "Arc Trail", List.of(), 0, List.of(battle.getId(), bears.getId()));

            harness.inMutationScope(() -> handler.resolve(gd, entry, effect));

            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
            assertThat(bears.getMarkedDamage()).isEqualTo(1);
        }

        /**
         * The any-target path used to remove defense counters in an arm of its own that returned
         * before the shared damage pipeline, so prevention, redirection, damage multipliers and
         * spell lifelink (CR 702.15b) were all skipped for a battle.
         */
        @Test
        @DisplayName("Any-target damage to a battle goes through the shared pipeline, so spell lifelink applies")
        void anyTargetDamageToBattleGainsLifeForALifelinkSpell() {
            harness.addToBattlefield(player1, new FiresongAndSunspeaker());
            Permanent battle = addBattle(5);
            int lifeBefore = gd.getLife(player1.getId());
            DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, new Shock(),
                    player1.getId(), "Shock", List.of());

            harness.inMutationScope(() ->
                    damageSupport.resolveAnyTargetDamage(gd, entry, battle.getId(), 3, false));

            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        }

        private Permanent addBattle(int defense) {
            Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, defense);
            return battle;
        }
    }

    private void castInvasion(UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfInnistrad()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
