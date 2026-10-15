package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreholdCommand.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class LoreholdCommandTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Spirit and grants own creatures power, indestructible, and haste")
    void createsSpiritAndBoostsCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, null, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("deals damage to any target and gives a different target player life")
    void damagesAnyTargetAndGivesTargetPlayerLife() {
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("requires the life-gain target to be a player")
    void lifeGainModeRejectsPermanentTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castCommand();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 2}, null,
                List.of(player2.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("sacrifices a permanent and then draws two cards")
    void sacrificesPermanentThenDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 3}, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("the newly created Spirit receives the second mode before later creatures enter")
    void boostsCreatedSpiritButNotLaterCreatures() {
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 0}, null, List.of());
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.HASTE)).isTrue();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        for (Permanent creature : List.of(laterCreature, opponentCreature)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        }
    }

    @Test
    @DisplayName("the Spirit created by the first mode can be sacrificed to the fourth mode")
    void sacrificesCreatedSpirit() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{3, 0}, null, List.of());
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, spirit.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spirit");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("draws two cards even when there is no permanent to sacrifice")
    void drawsWithoutPermanentToSacrifice() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, null, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("the same player can be targeted for damage and life gain")
    void damagesAndGivesLifeToSamePlayer() {
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("damage mode kills a creature while the other target gains life")
    void damagesCreatureAndGivesLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(creature.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("damage mode rejects a noncreature artifact")
    void damageModeRejectsNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        castCommand();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 2}, null,
                List.of(artifact.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("the boost and keyword grants expire at the end of the turn")
    void boostAndKeywordsExpire() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new HillGiant()));
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, null, List.of());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("the life gain and token modes still resolve if the damage target leaves")
    void resolvesWithOnlyLifeGainTargetLegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castCommand();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(creature.getId(), player1.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player1, "Spirit");
        harness.assertInHand(player2, "Hill Giant");
    }
    private void castCommand() {
        harness.setHand(player1, List.of(new LoreholdCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
