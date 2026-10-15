package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.c.ChangelingTitan;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NamelessInversion.class, AirElemental.class, GoblinEliteInfantry.class, GoblinKing.class,
        GrizzlyBears.class, ChangelingTitan.class, AmoeboidChangeling.class})
class NamelessInversionTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Gives target creature +3/-3")
    void appliesBoost() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4
        castOn(elemental);

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-3);
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 toughness kills a small creature")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        castOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Strips creature types, removing a tribal buff")
    void losesAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing()); // Goblins get +1/+1
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry()); // 2/2 -> 3/3

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // buffed by Goblin King

        castOn(goblin);

        // Base 2/2, loses Goblin type (King buff gone), then +3/-3 => 5/-1 => dies.
        harness.assertInGraveyard(player1, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("+3/-3 and type loss wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);
        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, elemental, CardSubtype.ELEMENTAL)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(0);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasEffectiveSubtype(gd, elemental, CardSubtype.ELEMENTAL)).isTrue();
    }

    @Test
    @DisplayName("Type loss overrides changeling without removing the ability")
    void stripsChangelingTypesUntilCleanup() {
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new ChangelingTitan());
        assertThat(gqs.hasEffectiveSubtype(gd, titan, CardSubtype.GOBLIN)).isTrue();

        castOn(titan);

        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, titan)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, titan)).isTrue();
        assertThat(gqs.hasKeyword(gd, titan, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, titan, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, titan, CardSubtype.SHAPESHIFTER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, titan)).isEqualTo(7);
        assertThat(gqs.hasEffectiveSubtype(gd, titan, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, titan, CardSubtype.SHAPESHIFTER)).isTrue();
    }

    @Test
    @DisplayName("A later type grant overrides the type loss")
    void laterTypeGrantOverridesTypeLoss() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        changeling.setSummoningSick(false);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);
        assertThat(gqs.hasEffectiveSubtype(gd, elemental, CardSubtype.ELEMENTAL)).isFalse();

        harness.activateAbility(player1, 0, 0, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, elemental, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, elemental, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }
    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
