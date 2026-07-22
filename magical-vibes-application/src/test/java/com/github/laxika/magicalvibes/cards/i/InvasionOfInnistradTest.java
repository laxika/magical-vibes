package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
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
            GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                    .checkAfterDefenseRemoved(gd, battle);

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

    private void castInvasion(UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfInnistrad()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
