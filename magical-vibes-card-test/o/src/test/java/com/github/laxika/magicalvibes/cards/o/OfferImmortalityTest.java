package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PhyrexianAtlas;
import com.github.laxika.magicalvibes.cards.b.BranchblightStalker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AnnihilatingGlare;
import com.github.laxika.magicalvibes.cards.f.FurnaceStrider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OfferImmortality.class, BranchblightStalker.class, PhyrexianAtlas.class, AnnihilatingGlare.class, FurnaceStrider.class})
class OfferImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants deathtouch and indestructible to the target creature")
    void resolvingGrantsBothKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        harness.setHand(player1, List.of(new OfferImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        harness.setHand(player1, List.of(new OfferImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canTargetOpponentsCreatureWithoutGrantingKeywordsToOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BranchblightStalker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        harness.setHand(player1, List.of(new OfferImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(other.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(other.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void indestructiblePreventsDestroyEffect() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        harness.setHand(player1, List.of(new OfferImmortality(), new AnnihilatingGlare()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Branchblight Stalker");
        harness.assertNotInGraveyard(player1, "Branchblight Stalker");
    }

    @Test
    void deathtouchKillsLargerBlockerWhileIndestructibleSavesAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FurnaceStrider());
        harness.setHand(player1, List.of(new OfferImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Branchblight Stalker");
        harness.assertNotOnBattlefield(player2, "Furnace Strider");
        harness.assertInGraveyard(player2, "Furnace Strider");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new PhyrexianAtlas());
        harness.setHand(player1, List.of(new OfferImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Phyrexian Atlas");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
