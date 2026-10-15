package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.r.RavenousDaggertooth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionsJudgment.class, ColossalDreadmaw.class, BishopsSoldier.class, RavenousDaggertooth.class})
class LegionsJudgmentTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Legion's Judgment targeting a creature with power 4+ puts it on stack")
    void castingPutsOnStack() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, wurm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(LegionsJudgment.class);
        assertThat(entry.getTargetId()).isEqualTo(wurm.getId());
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BishopsSoldier());

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a creature with exactly power 3")
    void cannotTargetPower3Creature() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Resolving Legion's Judgment destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, wurm.getId());

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player1, "Legion's Judgment");
    }

    @Test
    @DisplayName("Legion's Judgment allows regeneration")
    void allowsRegeneration() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        wurm.setRegenerationShield(1);

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, wurm.getId());

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Legion's Judgment fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, wurm.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Legion's Judgment");
    }

    @Test
    void destroysCreatureWithExactlyFourEffectivePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RavenousDaggertooth());
        target.setPowerModifier(1);
        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ravenous Daggertooth");
        harness.assertInGraveyard(player1, "Ravenous Daggertooth");
        harness.assertInGraveyard(player1, "Legion's Judgment");
    }

    @Test
    void doesNotDestroyTargetWhosePowerFallsBelowFourBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new LegionsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        target.setPowerModifier(-3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertNotInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player1, "Legion's Judgment");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
