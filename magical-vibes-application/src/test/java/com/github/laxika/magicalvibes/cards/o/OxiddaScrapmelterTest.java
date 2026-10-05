package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.cards.e.EzurisArchers;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OxiddaScrapmelter.class, AccordersShield.class, VulshokReplica.class, EzurisArchers.class})
class OxiddaScrapmelterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Oxidda Scrapmelter requires no target")
    void castingRequiresNoTarget() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Oxidda Scrapmelter");
        assertThat(entry.getTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Oxidda Scrapmelter enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Accorder's Shield");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Oxidda Scrapmelter");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Oxidda Scrapmelter");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Accorder's Shield");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Accorder's Shield");
        harness.assertInGraveyard(player2, "Accorder's Shield");
    }

    @Test
    @DisplayName("ETB destroys artifact creature")
    void etbDestroysArtifactCreature() {
        harness.addToBattlefield(player2, new VulshokReplica());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Vulshok Replica");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vulshok Replica");
        harness.assertInGraveyard(player2, "Vulshok Replica");
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new EzurisArchers());
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Ezuri's Archers");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Indestructible artifact survives ETB")
    void indestructibleArtifactSurvives() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Accorder's Shield");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell.
        harness.passBothPriorities();

        // Grant indestructible to the target before ETB resolves
        Permanent target = findPermanent(player2, "Accorder's Shield");
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Accorder's Shield");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("indestructible"));
    }

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Accorder's Shield");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell.
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can cast without a target when no artifacts on battlefield")
    void canCastWithoutTargetWhenNoArtifacts() {
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Oxidda Scrapmelter");
    }

    @Test
    @DisplayName("No ETB ability remains on the stack when there are no legal targets")
    void noEtbOnStackWithoutLegalTargets() {
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Oxidda Scrapmelter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mandatory ETB can destroy an artifact controlled by Scrapmelter's controller")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Accorder's Shield"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Accorder's Shield");
        harness.assertOnBattlefield(player1, "Oxidda Scrapmelter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact appearing before Scrapmelter enters can be targeted")
    void targetsArtifactAppearingBeforeEntry() {
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.addToBattlefield(player2, new AccordersShield());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Accorder's Shield"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Accorder's Shield");
        harness.assertOnBattlefield(player1, "Oxidda Scrapmelter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scrapmelter triggers when it enters without being cast")
    void triggersWithoutBeingCast() {
        harness.addToBattlefield(player2, new VulshokReplica());
        harness.enterBattlefieldAndReturn(player1, new OxiddaScrapmelter());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Vulshok Replica"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vulshok Replica");
        harness.assertOnBattlefield(player1, "Oxidda Scrapmelter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves independently after Scrapmelter leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Accorder's Shield"));

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Accorder's Shield");
        assertThat(gd.stack).isEmpty();
    }
}
